package com.trang.gachon.movie.service;

import com.trang.gachon.movie.entity.Account;
import com.trang.gachon.movie.entity.Invoice;
import com.trang.gachon.movie.entity.Member;
import com.trang.gachon.movie.entity.Ticket;
import com.trang.gachon.movie.enums.InvoiceStatus;
import com.trang.gachon.movie.enums.SeatStatus;
import com.trang.gachon.movie.repository.AccountRepository;
import com.trang.gachon.movie.repository.InvoiceRepository;
import com.trang.gachon.movie.repository.MemberRepository;
import com.trang.gachon.movie.repository.ScheduleSeatRepository;
import com.trang.gachon.movie.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingManageService {

    private final InvoiceRepository invoiceRepository;
    private final TicketRepository ticketRepository;
    private final ScheduleSeatRepository scheduleSeatRepository;
    private final MemberRepository memberRepository;
    private final AccountRepository accountRepository;

    // ============================================================
    // Danh sách booking cho nhân viên xem và xử lý
    // ============================================================
    public List<Invoice> getAll() {
        return invoiceRepository.findAllByOrderByBookingDateDesc();
    }

    // ============================================================
    // Search theo booking code / phone / CMND
    // ============================================================
    public List<Invoice> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }
        return invoiceRepository.searchByKeyword(keyword.trim());
    }

    // ============================================================
    // Lấy chi tiết invoice. Dùng query fetch account để detail page
    // đọc được thông tin khách hàng ổn định hơn.
    // ============================================================
    public Invoice getById(Long id) {
        return invoiceRepository.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy booking!"));
    }

    // ============================================================
    // WAITING -> CONFIRMED
    // Chỗ này chỉ đổi trạng thái vì ghế đã được giữ từ lúc member đặt vé.
    // ============================================================
    @Transactional
    public Invoice confirmBooking(Long id, String handledByUsername) {
        Invoice invoice = getById(id);
        ensureWaiting(invoice, "xác nhận");

        // Ghi lại ai là người đã xác nhận booking này tại quầy.
        invoice.setHandledBy(loadHandledByAccount(handledByUsername));
        invoice.setInvoiceStatus(InvoiceStatus.CONFIRMED);
        return invoiceRepository.save(invoice);
    }

    // ============================================================
    // WAITING -> CANCELLED
    // Khi hủy booking online, cần:
    // 1. trả ghế về AVAILABLE
    // 2. hoàn lại điểm đã dùng
    // 3. thu hồi điểm đã cộng trước đó
    // ============================================================
    @Transactional
    public Invoice cancelBooking(Long id, String handledByUsername) {
        Invoice invoice = getById(id);
        ensureWaiting(invoice, "hủy");

        List<Ticket> tickets = ticketRepository.findDetailedByInvoice_InvoiceId(invoice.getInvoiceId());
        tickets.forEach(ticket -> ticket.getScheduleSeat().setSeatStatus(SeatStatus.AVAILABLE));
        scheduleSeatRepository.saveAll(
                tickets.stream().map(Ticket::getScheduleSeat).toList()
        );

        rollbackMemberScore(invoice);

        // Hủy cũng là một hành động nghiệp vụ, nên vẫn lưu người xử lý để audit.
        invoice.setHandledBy(loadHandledByAccount(handledByUsername));
        invoice.setInvoiceStatus(InvoiceStatus.CANCELLED);
        return invoiceRepository.save(invoice);
    }

    private void ensureWaiting(Invoice invoice, String action) {
        if (invoice.getInvoiceStatus() == InvoiceStatus.CONFIRMED) {
            throw new IllegalArgumentException("Booking này đã được xác nhận rồi, không thể " + action + ".");
        }
        if (invoice.getInvoiceStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalArgumentException("Booking này đã bị hủy rồi.");
        }
    }

    private void rollbackMemberScore(Invoice invoice) {
        Account account = invoice.getAccount();
        if (account == null || account.getMember() == null) {
            return;
        }

        Member member = account.getMember();
        long currentScore = member.getScore() == null ? 0L : member.getScore();
        long usedScore = invoice.getUseScore() == null ? 0L : invoice.getUseScore();
        long addedScore = invoice.getAddScore() == null ? 0L : invoice.getAddScore();

        // Booking WAITING bị hủy thì hoàn lại điểm đã dùng.
        // Đồng thời thu hồi điểm đã cộng trước đó để số điểm quay về đúng trạng thái trước booking.
        long rolledBackScore = Math.max(0L, currentScore - addedScore) + usedScore;
        member.setScore(rolledBackScore);
        memberRepository.save(member);
    }

    private Account loadHandledByAccount(String username) {
        return accountRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("Không xác định được nhân viên đang xử lý booking!"));
    }
}
