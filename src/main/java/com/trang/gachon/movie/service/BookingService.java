package com.trang.gachon.movie.service;

import com.trang.gachon.movie.dto.BookingRequest;
import com.trang.gachon.movie.entity.*;
import com.trang.gachon.movie.enums.InvoiceStatus;
import com.trang.gachon.movie.enums.SeatStatus;
import com.trang.gachon.movie.enums.SeatType;
import com.trang.gachon.movie.enums.TicketType;
import com.trang.gachon.movie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final ShowTimeRepository     showtimeRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final InvoiceRepository      invoiceRepository;
    private final TicketRepository       ticketRepository;
    private final SeatRepository         seatRepository;
    private final MemberRepository       memberRepository;
    private final AccountRepository      accountRepository;
    private final PromotionRepository    promotionRepository;

    private static final long NORMAL_PRICE = 45_000L;
    private static final long VIP_PRICE    = 75_000L;

    // ============================================================
    // Lấy tất cả ghế của phòng (sắp xếp theo hàng + cột)
    // ============================================================
    public List<Seat> getSeatsByRoom(Long cinemaRoomId) {
        return seatRepository
                .findByCinemaRoom_CinemaRoomIdOrderBySeatRowAscSeatColumnAsc(cinemaRoomId);
    }

    // ============================================================
    // Lấy trạng thái ghế theo suất chiếu
    // ============================================================
    public List<ScheduleSeat> getSeatMap(Long showtimeId) {
        return scheduleSeatRepository.findByShowtime_ShowTimeId(showtimeId);
    }

    // ============================================================
    // Kiểm tra ghế còn trống
    // ============================================================
    public boolean isSeatsAvailable(List<Long> seatIds, Long showtimeId) {
        return seatIds.stream().allMatch(seatId -> {
            Optional<ScheduleSeat> ss = scheduleSeatRepository.findBySeat_SeatIdAndShowtime_ShowTimeId(seatId, showtimeId);
            return ss.isEmpty() || ss.get().getSeatStatus() == SeatStatus.AVAILABLE;
        });
    }

    // ============================================================
    // Lấy invoice theo booking code
    // ============================================================
    public Invoice getByBookingCode(String code) {
        return invoiceRepository.findByBookingCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé!"));
    }

    // ============================================================
    // Lấy danh sách ticket của invoice
    // ============================================================
    public List<Ticket> getTicketsByInvoice(Long invoiceId) {
        return ticketRepository.findByInvoice_InvoiceId(invoiceId);
    }

    // ============================================================
    // Tính tiền
    // ============================================================
    public long calculateTotal(List<Long> seatIds) {
        return seatIds.stream().mapToLong(seatId -> {
            Seat seat = seatRepository.findById(seatId).orElseThrow();
            return seat.getSeatType() == SeatType.VIP ? VIP_PRICE : NORMAL_PRICE;
        }).sum();
    }

    // ============================================================
    // Confirm đặt vé — tạo Invoice + Ticket per ghế
    // ============================================================
    @Transactional
    public Invoice confirmBooking(BookingRequest req, String username) {

        // 1. Lấy showtime
        Showtime showtime = showtimeRepository.findById(req.getShowtimeId())
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại!"));

        // 2. Kiểm tra ghế còn trống
        if (!isSeatsAvailable(req.getSeatIds(), req.getShowtimeId()))
            throw new IllegalArgumentException(
                    "Một số ghế đã được đặt! Vui lòng chọn lại.");

        // 3. Lấy account
        Account account = accountRepository.findByUserName(username).orElseThrow();

        // 4. Lấy hoặc tạo ScheduleSeat
        List<ScheduleSeat> scheduleSeats = req.getSeatIds().stream()
                .map(seatId -> scheduleSeatRepository
                        .findBySeat_SeatIdAndShowtime_ShowTimeId(seatId, req.getShowtimeId())
                        .orElseGet(() -> {
                            Seat seat = seatRepository.findById(seatId).orElseThrow();
                            return scheduleSeatRepository.save(ScheduleSeat.builder()
                                    .seat(seat).showtime(showtime)
                                    .seatStatus(SeatStatus.AVAILABLE)
                                    .build());
                        }))
                .collect(Collectors.toList());

        // 5. Tính subTotal
        long subTotal = scheduleSeats.stream()
                .mapToLong(ss -> ss.getSeat().getSeatType() == SeatType.VIP
                        ? VIP_PRICE : NORMAL_PRICE)
                .sum();

        // 6. Áp promotion
        long discountAmount = 0L;
        Promotion promotion = null;

        if (req.getPromotionId() != null) {
            promotion = promotionRepository.findById(req.getPromotionId()).orElse(null);
            if (promotion != null && promotion.isValid())
                discountAmount = promotion.getDiscountLevel() != null
                        ? promotion.getDiscountLevel() : 0L;
        }

        long total    = Math.max(0, subTotal - discountAmount);
        long useScore = 0L;
        long addScore = 0L;

        // 7. Dùng điểm quy đổi
        if (req.isUseScore() && account.getMember() != null) {
            Member member = account.getMember();
            if (member.getScore() < total)
                throw new IllegalArgumentException("Điểm không đủ để quy đổi!");
            useScore = total;
            total    = 0L;
            member.setScore(member.getScore() - useScore);
            memberRepository.save(member);
        }

        // 8. Cộng điểm 10%
        addScore = total / 10;
        if (account.getMember() != null) {
            Member member = account.getMember();
            member.setScore(member.getScore() + addScore);
            memberRepository.save(member);
        }

        // 9. Snapshot tên ghế
        String seatSnapshot = scheduleSeats.stream()
                .map(ss -> ss.getSeat().getSeatRow() + ss.getSeat().getSeatColumn())
                .sorted()
                .collect(Collectors.joining(" "));

        Movie      movie = showtime.getMovie();
        CinemaRoom room  = showtime.getCinemaRoom();

        // 10. Tạo Invoice
        Invoice invoice = Invoice.builder()
                .bookingCode(generateBookingCode())
                .movieName(movie.getMovieNameVn() != null
                        ? movie.getMovieNameVn() : movie.getMovieNameEnglish())
                .cinemaRoom(room.getCinemaRoomName())
                .scheduleShow(showtime.getShowDate())
                .scheduleShowTime(String.valueOf(showtime.getShowTime()))
                .seat(seatSnapshot)
                .bookingDate(LocalDateTime.now())
                .invoiceStatus(InvoiceStatus.WAITING)
                .totalMoney(total)
                .addScore(addScore)
                .useScore(useScore)
                .account(account)
                .promotion(promotion)
                .build();

        invoiceRepository.save(invoice);

        // 11. Tạo Ticket line item + đổi ghế → SOLD
        Invoice finalInvoice = invoice;
        scheduleSeats.forEach(ss -> {
            long price = ss.getSeat().getSeatType() == SeatType.VIP
                    ? VIP_PRICE : NORMAL_PRICE;
            TicketType type = ss.getSeat().getSeatType() == SeatType.VIP
                    ? TicketType.VIP : TicketType.NORMAL;

            ticketRepository.save(Ticket.builder()
                    .price(price).ticketType(type)
                    .invoice(finalInvoice).scheduleSeat(ss)
                    .build());

            ss.setSeatStatus(SeatStatus.SOLD);
            scheduleSeatRepository.save(ss);
        });

        return invoice;
    }

    // ============================================================
    // Helper
    // ============================================================
    private String generateBookingCode() {
        String code;
        do {
            code = UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 10).toUpperCase();
        } while (invoiceRepository.findByBookingCode(code).isPresent());
        return code;
    }
}