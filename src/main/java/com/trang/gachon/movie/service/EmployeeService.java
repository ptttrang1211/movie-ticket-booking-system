package com.trang.gachon.movie.service;

import com.trang.gachon.movie.dto.EmployeeRequest;
import com.trang.gachon.movie.entity.Account;
import com.trang.gachon.movie.entity.Employee;
import com.trang.gachon.movie.entity.Role;
import com.trang.gachon.movie.enums.AccountStatus;
import com.trang.gachon.movie.repository.AccountRepository;
import com.trang.gachon.movie.repository.EmployeeRepository;
import com.trang.gachon.movie.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Employee> getActiveEmployees() {
        // Chỉ lấy những employee có account còn ACTIVE để admin nhìn đúng danh sách đang làm việc.
        return employeeRepository.findAll().stream()
                .filter(emp -> emp.getAccount() != null
                        && emp.getAccount().getAccountStatus() == AccountStatus.ACTIVE)
                .sorted(Comparator.comparing(Employee::getEmployeeId))
                .toList();
    }

    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên!"));
    }

    @Transactional
    public void addEmployee(EmployeeRequest req) {
        validateRequiredForCreate(req);
        validateUniqueForCreate(req);

        Role employeeRole = roleRepository.findByRoleName("EMPLOYEE")
                .orElseThrow(() -> new IllegalArgumentException("Role EMPLOYEE không tồn tại!"));

        // Tạo account trước vì account là gốc để employee dùng login/phân quyền.
        Account account = Account.builder()
                .userName(req.getUserName().trim())
                .password(passwordEncoder.encode(req.getPassword().trim()))
                .fullName(req.getFullName().trim())
                .email(req.getEmail().trim())
                .phoneNumber(req.getPhoneNumber().trim())
                .identityCard(req.getIdentityCard().trim())
                .gender(req.getGender())
                .dateOfBirth(req.getDateOfBirth())
                .address(req.getAddress())
                .registerDate(LocalDate.now())
                .accountStatus(AccountStatus.ACTIVE)
                .role(employeeRole)
                .build();

        accountRepository.save(account);

        employeeRepository.save(Employee.builder()
                .account(account)
                .build());
    }

    @Transactional
    public void updateEmployee(Long id, EmployeeRequest req) {
        Employee employee = getById(id);
        Account account = employee.getAccount();

        validateUniqueForUpdate(req, account);

        account.setUserName(req.getUserName().trim());
        account.setFullName(req.getFullName().trim());
        account.setEmail(req.getEmail().trim());
        account.setPhoneNumber(req.getPhoneNumber().trim());
        account.setIdentityCard(req.getIdentityCard().trim());
        account.setGender(req.getGender());
        account.setDateOfBirth(req.getDateOfBirth());
        account.setAddress(req.getAddress());

        // Edit không bắt buộc đổi password.
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(req.getPassword().trim()));
        }

        accountRepository.save(account);
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = getById(id);

        // Không xóa cứng employee để còn giữ lịch sử giao dịch / thông tin audit.
        employee.getAccount().setAccountStatus(AccountStatus.LOCKED);
        accountRepository.save(employee.getAccount());
    }

    private void validateRequiredForCreate(EmployeeRequest req) {
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống!");
        }
    }

    private void validateUniqueForCreate(EmployeeRequest req) {
        if (accountRepository.existsByUserName(req.getUserName().trim())) {
            throw new IllegalArgumentException("Username đã tồn tại!");
        }
        if (accountRepository.existsByEmail(req.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }
        if (accountRepository.existsByPhoneNumber(req.getPhoneNumber().trim())) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
        }
        if (accountRepository.existsByIdentityCard(req.getIdentityCard().trim())) {
            throw new IllegalArgumentException("CMND/CCCD đã tồn tại!");
        }
    }

    private void validateUniqueForUpdate(EmployeeRequest req, Account currentAccount) {
        accountRepository.findByUserName(req.getUserName().trim())
                .filter(acc -> !acc.getAccountId().equals(currentAccount.getAccountId()))
                .ifPresent(acc -> {
                    throw new IllegalArgumentException("Username đã tồn tại!");
                });

        accountRepository.findByEmail(req.getEmail().trim())
                .filter(acc -> !acc.getAccountId().equals(currentAccount.getAccountId()))
                .ifPresent(acc -> {
                    throw new IllegalArgumentException("Email đã tồn tại!");
                });

        accountRepository.findByPhoneNumber(req.getPhoneNumber().trim())
                .filter(acc -> !acc.getAccountId().equals(currentAccount.getAccountId()))
                .ifPresent(acc -> {
                    throw new IllegalArgumentException("Số điện thoại đã tồn tại!");
                });

        accountRepository.findByIdentityCard(req.getIdentityCard().trim())
                .filter(acc -> !acc.getAccountId().equals(currentAccount.getAccountId()))
                .ifPresent(acc -> {
                    throw new IllegalArgumentException("CMND/CCCD đã tồn tại!");
                });
    }
}
