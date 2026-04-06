package com.trang.gachon.movie.repository;

import com.trang.gachon.movie.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    // Dùng khi register: tìm role "MEMBER" để gán cho tài khoản mới
    Optional<Role> findByRoleName(String roleName);

}
