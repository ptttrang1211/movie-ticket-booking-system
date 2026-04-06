package com.trang.gachon.movie.service;

import com.trang.gachon.movie.dto.AccountUpdateRequest;
import com.trang.gachon.movie.entity.*;
import com.trang.gachon.movie.repository.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;

    // ---- Lấy thông tin account đang login ----
    public Account getByUsername(String username) {
        return accountRepository.findByUserName(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản!"));
    }

    // ---- Lấy thông tin member của account ----
    public Member getMemberByAccountId(Long accountId) {
        return memberRepository.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy member!"));
    }

    // ---- Lấy member nếu account hiện tại thực sự là member ----
    public Optional<Member> findMemberByAccountId(Long accountId) {
        return memberRepository.findByAccount_AccountId(accountId);
    }

    // ---- Cập nhật thông tin cá nhân ----
    @Transactional
    public void updateProfile(String username, AccountUpdateRequest req) {
        Account account = getByUsername(username);

        account.setFullName(req.getFullName());
        account.setEmail(req.getEmail());
        account.setPhoneNumber(req.getPhoneNumber());
        account.setGender(req.getGender());
        account.setAddress(req.getAddress());
        account.setDateOfBirth(req.getDateOfBirth());

        accountRepository.save(account);
    }

    // ---- Đổi mật khẩu ----
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        Account account = getByUsername(username);

        if (!passwordEncoder.matches(oldPassword, account.getPassword()))
            throw new IllegalArgumentException("Mật khẩu cũ không đúng!");

        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    // ---- Lịch sử đặt vé ----
    public List<Invoice> getBookingHistory(Long accountId) {
        return invoiceRepository.findByAccount_AccountIdOrderByBookingDateDesc(accountId);
    }

    // ---- Lịch sử điểm (add_score + use_score) ----
    public List<Invoice> getScoreHistory(Long accountId) {
        return invoiceRepository
                .findByAccount_AccountIdOrderByBookingDateDesc(accountId)
                .stream()
                .filter(i -> i.getAddScore() > 0 || i.getUseScore() > 0)
                .toList();
    }
}
