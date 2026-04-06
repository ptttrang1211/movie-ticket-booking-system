package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Ticket;
import com.trang.gachon.movie.enums.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {
    // Tra giá khi tính tiền: NORMAL → 45000, VIP → 75000
    Optional<Ticket> findByTicketType(TicketType ticketType);


    // Lấy tất cả ticket của 1 invoice
    List<Ticket> findByInvoice_InvoiceId(Long invoiceId);

    // Detail page cần đi sâu tới scheduleSeat -> seat, nên fetch sẵn để ổn định hơn.
    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.scheduleSeat ss " +
            "JOIN FETCH ss.seat " +
            "WHERE t.invoice.invoiceId = :invoiceId")
    List<Ticket> findDetailedByInvoice_InvoiceId(@Param("invoiceId") Long invoiceId);

    // Lấy ticket theo scheduleSeat (biết ghế nào đã bán ở suất nào)
    List<Ticket> findByScheduleSeat_ScheduleSeatId(Long scheduleSeatId);




}
