package com.trang.gachon.movie.service.authandotpservice;

import com.trang.gachon.movie.entity.Account;
import com.trang.gachon.movie.enums.AccountStatus;
import com.trang.gachon.movie.repository.AccountRepository;
import com.trang.gachon.movie.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingAccountCleanupScheduler {

    private final AccountRepository accountRepository;
    private final MemberRepository memberRepository;
    private final PendingAccountPolicy pendingAccountPolicy;


     //Chỉ dọn các account PENDING đã quá hạn; không đụng vào account ACTIVE
    @Transactional
    @Scheduled(fixedDelayString = "${app.pending.cleanup-interval-ms:600000}")
    public void cleanupExpiredPendingAccounts() {
        List<Account> pendingAccounts = accountRepository.findAllByAccountStatus(AccountStatus.PENDING);

        for (Account account : pendingAccounts) {
            if (!pendingAccountPolicy.isExpired(account)) {
                continue;
            }

            memberRepository.findByAccount_AccountId(account.getAccountId())
                    .ifPresent(memberRepository::delete);
            accountRepository.delete(account);
            log.info("Deleted expired pending account: {}", account.getEmail());
        }
    }
}
