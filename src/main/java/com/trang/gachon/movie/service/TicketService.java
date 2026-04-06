package com.trang.gachon.movie.service;

import com.trang.gachon.movie.dto.TicketSellRequest;
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
public class TicketService {

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
    // Lấy tất cả ghế của phòng
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
    // Tìm member theo memberCode hoặc identityCard
    // ============================================================
    public Optional<Member> findMember(String keyword) {
        Optional<Member> byCode = memberRepository.findByMemberCode(keyword);
        if (byCode.isPresent()) return byCode;
        return memberRepository.findByAccount_IdentityCard(keyword);
    }

    // ============================================================
    // Lấy invoice theo booking code
    // ============================================================
    public Invoice getByBookingCode(String code) {
        return invoiceRepository.findDetailByBookingCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé!"));
    }

    // ============================================================
    // Lấy danh sách ticket của invoice
    // ============================================================
    public List<Ticket> getTicketsByInvoice(Long invoiceId) {
        return ticketRepository.findDetailedByInvoice_InvoiceId(invoiceId);
    }

    // ============================================================
    // Employee bán vé tại quầy — CONFIRMED ngay
    // ============================================================
    @Transactional
    public Invoice sellTicket(TicketSellRequest req, String handledByUsername) {
        if (req.getSeatIds() == null || req.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất 1 ghế!");
        }

        // 1. Lấy showtime
        Showtime showtime = showtimeRepository.findById(req.getShowtimeId())
                .orElseThrow(() -> new IllegalArgumentException("Suất chiếu không tồn tại!"));

        // 2. Lấy hoặc tạo ScheduleSeat
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

        boolean hasUnavailableSeat = scheduleSeats.stream()
                .anyMatch(ss -> ss.getSeatStatus() != SeatStatus.AVAILABLE);
        if (hasUnavailableSeat) {
            throw new IllegalArgumentException("Một hoặc nhiều ghế đã được bán hoặc đang không khả dụng!");
        }

        // 3. Tính subTotal
        long subTotal = scheduleSeats.stream()
                .mapToLong(ss -> ss.getSeat().getSeatType() == SeatType.VIP
                        ? VIP_PRICE : NORMAL_PRICE)
                .sum();

        // 4. Áp promotion
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
        String keyword = req.getMemberKeyword() == null ? "" : req.getMemberKeyword().trim();

        // 5. Xử lý member nếu có
        Member member = null;
        Account account = null;

        if (!keyword.isBlank()) {
            member = findMember(keyword)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên!"));
            account = member.getAccount();
        }

        // Dùng điểm chỉ hợp lệ khi đã tra cứu được member
        if (req.isUseScore()) {
            if (member == null) {
                throw new IllegalArgumentException("Vui lòng tra cứu thành viên trước khi dùng điểm!");
            }
            if (member.getScore() < total) {
                throw new IllegalArgumentException("Điểm không đủ để quy đổi!");
            }
            useScore = total;
            total    = 0L;
            member.setScore(member.getScore() - useScore);
        }

        // Chỉ cộng điểm khi vé được bán cho member
        if (member != null) {
            addScore = total / 10;
            member.setScore(member.getScore() + addScore);
            memberRepository.save(member);
        }

        // 6. Snapshot tên ghế
        String seatSnapshot = scheduleSeats.stream()
                .map(ss -> ss.getSeat().getSeatRow() + ss.getSeat().getSeatColumn())
                .sorted()
                .collect(Collectors.joining(" "));

        Movie      movie = showtime.getMovie();
        CinemaRoom room  = showtime.getCinemaRoom();
        Account handledBy = loadHandledByAccount(handledByUsername);

        // 7. Tạo Invoice — CONFIRMED ngay (bán tại quầy)
        // handledBy là nhân viên đang thao tác ở quầy.
        Invoice invoice = Invoice.builder()
                .bookingCode(generateBookingCode())
                .movieName(movie.getMovieNameVn() != null
                        ? movie.getMovieNameVn() : movie.getMovieNameEnglish())
                .cinemaRoom(room.getCinemaRoomName())
                .scheduleShow(showtime.getShowDate())
                .scheduleShowTime(String.valueOf(showtime.getShowTime()))
                .seat(seatSnapshot)
                .bookingDate(LocalDateTime.now())
                .invoiceStatus(InvoiceStatus.CONFIRMED)
                .totalMoney(total)
                .addScore(addScore)
                .useScore(useScore)
                .account(account)
                .handledBy(handledBy)
                .promotion(promotion)
                .build();

        invoiceRepository.save(invoice);

        // 8. Tạo Ticket line item + đổi ghế → SOLD
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

    private Account loadHandledByAccount(String username) {
        return accountRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("Không xác định được nhân viên đang bán vé!"));
    }
}
