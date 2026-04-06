package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Invoice;
import com.trang.gachon.movie.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice,Long> {
    // Danh sách invoice cho màn booking-manage
    List<Invoice> findAllByOrderByBookingDateDesc();

    //lịch sử đặt vé của 1 member
    List<Invoice> findByAccount_AccountIdOrderByBookingDateDesc(Long accountId);

    //tìm theo booking code
    Optional<Invoice> findByBookingCode(String bookingCode);

    //tìm theo số điện thoại
    List<Invoice> findByAccount_PhoneNumberContaining(String phoneNumber);
    
    // Tìm theo CMND
    List<Invoice> findByAccount_IdentityCardContaining(String identityCard);

    // Lọc theo trạng thái
    List<Invoice> findByInvoiceStatusOrderByBookingDateDesc(InvoiceStatus invoiceStatus);

    // Chi tiết invoice cho employee xử lý xác nhận / hủy
    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.account " +
            "LEFT JOIN FETCH i.handledBy " +
            "WHERE i.invoiceId = :id")
    Optional<Invoice> findDetailById(@Param("id") Long id);

    @Query("SELECT i FROM Invoice i " +
            "LEFT JOIN FETCH i.account " +
            "LEFT JOIN FETCH i.handledBy " +
            "WHERE i.bookingCode = :code")
    Optional<Invoice> findDetailByBookingCode(@Param("code") String code);

    // Tìm kiếm cho trang booking management (Employee)
    @Query("SELECT i FROM Invoice i LEFT JOIN i.account a WHERE " +
            "i.bookingCode LIKE %:kw% OR " +
            "a.phoneNumber LIKE %:kw% OR " +
            "a.identityCard LIKE %:kw% " +
            "ORDER BY i.bookingDate DESC")
    List<Invoice> searchByKeyword(@Param("kw") String keyword);

}
