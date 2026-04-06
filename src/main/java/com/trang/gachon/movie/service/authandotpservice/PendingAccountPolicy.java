package com.trang.gachon.movie.service.authandotpservice;

import com.trang.gachon.movie.entity.Account;
import com.trang.gachon.movie.enums.AccountStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class PendingAccountPolicy {

    @Value("${app.pending.expire-hours:1}")
    private long expireHours;

    //Chỉ tài khoản PENDING mới có hạn sống. ACTIVE là tài khoản chính thức nên không áp dụng timeout
    public boolean isExpired(Account account) {
        return account != null
                && account.getAccountStatus() == AccountStatus.PENDING
                && account.getPendingCreatedAt() != null
                && LocalDateTime.now().isAfter(account.getPendingCreatedAt().plusHours(expireHours));
        //khi test thì chuyển sang plusMinutes để dễ test hơn
    }
}
