package com.trang.gachon.movie.service.authandotpservice;


import com.trang.gachon.movie.dto.*;
import com.trang.gachon.movie.entity.*;
import com.trang.gachon.movie.enums.AccountStatus;
import com.trang.gachon.movie.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository  accountRepository;
    private final MemberRepository memberRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder  passwordEncoder;
    private final RoleRepository roleRepository;
    private final OtpService        otpService;
    private final PendingAccountPolicy pendingAccountPolicy;

    // ============================================================
    // REGISTER — Bước 1: validate + lưu DB, với status PENDING, chờ verify OTP
    // ============================================================
    @Transactional
    public String register(RegisterRequest req) {

        // Validate
        if (!req.getPassword().equals(req.getConfirmPassword()))
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");

        cleanupExpiredPendingConflicts(req);

        Role memberRole = roleRepository.findByRoleName("MEMBER")
                .orElseThrow(() -> new IllegalArgumentException("Role MEMBER không tồn tại!"));

        Optional<Account> existingByEmail = accountRepository.findByEmail(req.getEmail());
        if (existingByEmail.isPresent()) {
            Account existingAccount = existingByEmail.get();
            if (existingAccount.getAccountStatus() != AccountStatus.PENDING) {
                throw new IllegalArgumentException("Email đã được sử dụng!");
            }

            validateUniqueForPendingUpdate(req, existingAccount);
            applyRegisterData(existingAccount, req, memberRole);
            accountRepository.save(existingAccount);
            otpService.generateAndSend(existingAccount.getEmail());
            return "Bạn đã có đăng ký chưa xác thực. Thông tin đã được cập nhật, vui lòng nhập mã OTP mới.";
        }

        validateUniqueForNewAccount(req);

        Account account = new Account();
        applyRegisterData(account, req, memberRole);
        accountRepository.save(account);

        // Gửi OTP xác thực email
        otpService.generateAndSend(req.getEmail());
        return "Đăng ký thành công! Vui lòng nhập mã OTP được gửi về email.";
    }

    // ============================================================
    // VERIFY OTP — xác thực email, đổi PENDING → ACTIVE
    //OTP đúng thì chuyển account từ PENDING sang ACTIVE, đồng thời xóa mốc thời gian pending để account không còn bị cleanup
    // ============================================================
    @Transactional
    public boolean verifyOtp(String email, String otp) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống!"));

        if (account.getAccountStatus() == AccountStatus.PENDING && pendingAccountPolicy.isExpired(account)) {
            deletePendingAccount(account);
            throw new IllegalArgumentException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
        }

        boolean valid = otpService.verify(email, otp);
        if (!valid) {
            return false;
        }

        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setPendingCreatedAt(null);
        accountRepository.save(account);

        if ("MEMBER".equals(account.getRole().getRoleName())
                && memberRepository.findByAccount_AccountId(account.getAccountId()).isEmpty()) {
            memberRepository.save(Member.builder()
                    .account(account)
                    .memberCode(generateMemberCode())
                    .score(0L)
                    .build());
        }

        return true;
    }

    // RESEND OTP — gửi lại OTP (dùng cho cả verify sau register
    //              và forgot password)
    // ============================================================
    public void resendOtp(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống!"));

        if (account.getAccountStatus() == AccountStatus.PENDING && pendingAccountPolicy.isExpired(account)) {
            deletePendingAccount(account);
            throw new IllegalArgumentException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
        }

        // Nếu đã ACTIVE thì không cần verify nữa
        if (account.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Tài khoản đã được xác thực rồi!");
        }

        otpService.generateAndSend(email);
    }


    // ============================================================
    // FORGOT PASSWORD — Gửi OTP về email để reset
    // ============================================================
    public void sendResetOtp(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống!"));

        if (account.getAccountStatus() == AccountStatus.PENDING) {
            if (pendingAccountPolicy.isExpired(account)) {
                deletePendingAccount(account);
                throw new IllegalArgumentException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            }
            throw new IllegalArgumentException("Tài khoản chưa xác thực email. Vui lòng xác thực trước.");
        }

        otpService.generateAndSend(email);
    }
    

    // ============================================================
    // RESET PASSWORD — Xác thực OTP + đặt mật khẩu mới
    // ============================================================
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        Account account = accountRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại!"));

        if (account.getAccountStatus() == AccountStatus.PENDING) {
            if (pendingAccountPolicy.isExpired(account)) {
                deletePendingAccount(account);
                throw new IllegalArgumentException("Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            }
            throw new IllegalArgumentException("Tài khoản chưa xác thực email. Vui lòng xác thực trước.");
        }

        // Verify OTP
        if (!otpService.verify(req.getEmail(), req.getOtp()))
            throw new IllegalArgumentException("Mã OTP không đúng hoặc đã hết hạn!");

        if (!req.getNewPassword().equals(req.getConfirmPassword()))
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");

        account.setPassword(passwordEncoder.encode(req.getNewPassword()));
        accountRepository.save(account);
    }


    // ============================================================
    // Helper
    // ============================================================
    private void cleanupExpiredPendingConflicts(RegisterRequest req) {
        Map<Long, Account> candidates = new LinkedHashMap<>();
        accountRepository.findByEmail(req.getEmail())
                .ifPresent(account -> candidates.put(account.getAccountId(), account));
        accountRepository.findByUserName(req.getUsername())
                .ifPresent(account -> candidates.put(account.getAccountId(), account));
        accountRepository.findByPhoneNumber(req.getPhoneNumber())
                .ifPresent(account -> candidates.put(account.getAccountId(), account));
        accountRepository.findByIdentityCard(req.getIdentityCard())
                .ifPresent(account -> candidates.put(account.getAccountId(), account));

        for (Account account : candidates.values()) {
            if (pendingAccountPolicy.isExpired(account)) {
                deletePendingAccount(account);
            }
        }
    }

    private void validateUniqueForNewAccount(RegisterRequest req) {
        if (accountRepository.findByUserName(req.getUsername()).isPresent())
            throw new IllegalArgumentException("Tên tài khoản đã tồn tại!");

        if (accountRepository.findByIdentityCard(req.getIdentityCard()).isPresent())
            throw new IllegalArgumentException("Số CMND/CCCD đã được đăng ký!");

        if (accountRepository.findByPhoneNumber(req.getPhoneNumber()).isPresent())
            throw new IllegalArgumentException("Số điện thoại đã được đăng ký!");
    }

    private void validateUniqueForPendingUpdate(RegisterRequest req, Account currentAccount) {
        accountRepository.findByUserName(req.getUsername())
                .filter(account -> !account.getAccountId().equals(currentAccount.getAccountId()))
                .ifPresent(account -> {
                    throw new IllegalArgumentException("Tên tài khoản đã tồn tại!");
                });

        accountRepository.findByIdentityCard(req.getIdentityCard())
                .filter(account -> !account.getAccountId().equals(currentAccount.getAccountId()))
                .ifPresent(account -> {
                    throw new IllegalArgumentException("Số CMND/CCCD đã được đăng ký!");
                });

        accountRepository.findByPhoneNumber(req.getPhoneNumber())
                .filter(account -> !account.getAccountId().equals(currentAccount.getAccountId()))
                .ifPresent(account -> {
                    throw new IllegalArgumentException("Số điện thoại đã được đăng ký!");
                });
    }

    private void applyRegisterData(Account account, RegisterRequest req, Role memberRole) {
        account.setUserName(req.getUsername());
        account.setPassword(passwordEncoder.encode(req.getPassword()));
        account.setFullName(req.getFullName());
        account.setEmail(req.getEmail());
        account.setPhoneNumber(req.getPhoneNumber());
        account.setIdentityCard(req.getIdentityCard());
        account.setGender(req.getGender());
        account.setDateOfBirth(req.getDateOfBirth());
        account.setAddress(req.getAddress());
        account.setRegisterDate(LocalDate.now());
        account.setPendingCreatedAt(LocalDateTime.now());
        account.setAccountStatus(AccountStatus.PENDING);
        account.setRole(memberRole);
    }

    private void deletePendingAccount(Account account) {
        memberRepository.findByAccount_AccountId(account.getAccountId())
                .ifPresent(memberRepository::delete);
        accountRepository.delete(account);
    }

    private String generateMemberCode() {
        String code;
        do {
            code = UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 10);
        } while (memberRepository.findByMemberCode(code).isPresent());
        return code;
    }
}
   //xem lại
