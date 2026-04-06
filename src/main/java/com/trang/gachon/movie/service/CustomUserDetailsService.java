package com.trang.gachon.movie.service;

import com.trang.gachon.movie.entity.Account;
import com.trang.gachon.movie.enums.AccountStatus;
import com.trang.gachon.movie.repository.AccountRepository;
import com.trang.gachon.movie.service.authandotpservice.PendingAccountPolicy;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security gọi loadUserByUsername() tự động khi user submit form login.
 * Không cần gọi thủ công trong controller.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PendingAccountPolicy pendingAccountPolicy;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        // 1. Tìm account trong DB
        Account account = accountRepository.findByUserName(login)
                .or(() -> accountRepository.findByEmail(login))
                .orElseThrow(() ->
                        new UsernameNotFoundException("User / password is invalid. Please try again!"));

        if (account.getAccountStatus() == AccountStatus.PENDING) {
            if (pendingAccountPolicy.isExpired(account)) {
                throw new DisabledException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            }
            throw new DisabledException("Tài khoản chưa xác thực email!");
        }

        // 2. Kiểm tra bị khóa
        if (account.getAccountStatus() == AccountStatus.LOCKED) {
            throw new DisabledException("Account has been locked!");
        }

        // 3. Gán role dạng "ROLE_ADMIN", "ROLE_MEMBER" ... (Spring Security yêu cầu prefix ROLE_)
        String roleWithPrefix = "ROLE_" + account.getRole().getRoleName();

        return User.builder()
                .username(account.getUserName())
                .password(account.getPassword())  // đã được BCrypt hash
                .authorities(List.of(new SimpleGrantedAuthority(roleWithPrefix)))
                .build();
    }
}
