package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Account;
import com.trang.gachon.movie.enums.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    // Dùng trong CustomUserDetailsService để load user khi login
    Optional<Account> findByUserName(String userName);
    
    Optional<Account> findByEmail(String email);
    Optional<Account> findByPhoneNumber(String phoneNumber);
    Optional<Account> findByIdentityCard(String identityCard);
    List<Account> findAllByAccountStatus(AccountStatus accountStatus);

    // Kiểm tra trùng khi đăng ký
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByIdentityCard(String identityCard);
    boolean existsByPhoneNumber(String  phoneNumber);
    
}
