package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    // Tìm Member theo account_id (dùng khi xem điểm, quản lý vé)
    Optional<Member> findByAccount_AccountId(Long accountId);
    Optional<Member> findByMemberCode(String memberCode);;
    Optional<Member> findByAccount_IdentityCard(String identityCard);
}
