package com.trang.gachon.movie.service;

import com.trang.gachon.movie.dto.BranchRequest;
import com.trang.gachon.movie.entity.Branch;
import com.trang.gachon.movie.enums.BranchStatus;
import com.trang.gachon.movie.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    // ---- Public: chỉ lấy chi nhánh đang hoạt động ----
    public List<Branch> getActiveBranches() {
        return branchRepository.findByBranchStatusOrderByBranchNameAsc(BranchStatus.ACTIVE);
    }

    // ---- Booking: chỉ lấy chi nhánh thực sự đang có lịch chiếu cho phim ----
    public List<Branch> getActiveBranchesByMovie(Long movieId) {
        return branchRepository.findActiveBranchesByMovie(
                movieId,
                LocalDate.now(),
                BranchStatus.ACTIVE
        );
    }

    // ---- Admin: lấy tất cả ----
    public List<Branch> getAll() {
        return branchRepository.findAllByOrderByBranchNameAsc();
    }

    public List<Branch> getAllForAdmin() {
        return branchRepository.findAllByOrderByBranchNameAsc();
    }

    public Branch getById(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi nhánh!"));
    }

    public BranchRequest toRequest(Long id) {
        Branch branch = getById(id);
        BranchRequest request = new BranchRequest();
        request.setBranchName(branch.getBranchName());
        request.setAddress(branch.getAddress());
        request.setPhone(branch.getPhone());
        request.setEmail(branch.getEmail());
        request.setImage(branch.getImage());
        request.setMapEmbedUrl(branch.getMapEmbedUrl());
        request.setBranchStatus(branch.getBranchStatus());
        return request;
    }

    @Transactional
    public void save(Branch branch) {
        branchRepository.save(branch);
    }

    @Transactional
    public Branch addBranch(BranchRequest request) {
        validateUniqueName(request.getBranchName(), null);

        // Tạo chi nhánh mới. Branch là thực thể gốc của cinema room,
        // nên phần này mình giữ độc lập, không sinh dữ liệu con tự động.
        Branch branch = Branch.builder()
                .branchName(request.getBranchName().trim())
                .address(request.getAddress().trim())
                .phone(blankToNull(request.getPhone()))
                .email(blankToNull(request.getEmail()))
                .image(blankToNull(request.getImage()))
                .mapEmbedUrl(blankToNull(request.getMapEmbedUrl()))
                .branchStatus(request.getBranchStatus() == null ? BranchStatus.ACTIVE : request.getBranchStatus())
                .build();

        return branchRepository.save(branch);
    }

    @Transactional
    public Branch updateBranch(Long id, BranchRequest request) {
        Branch branch = getById(id);
        validateUniqueName(request.getBranchName(), id);

        branch.setBranchName(request.getBranchName().trim());
        branch.setAddress(request.getAddress().trim());
        branch.setPhone(blankToNull(request.getPhone()));
        branch.setEmail(blankToNull(request.getEmail()));
        branch.setImage(blankToNull(request.getImage()));
        branch.setMapEmbedUrl(blankToNull(request.getMapEmbedUrl()));
        branch.setBranchStatus(request.getBranchStatus() == null ? BranchStatus.ACTIVE : request.getBranchStatus());

        return branchRepository.save(branch);
    }

    @Transactional
    public void delete(Long id) {
        branchRepository.deleteById(id);
    }

    @Transactional
    public void deleteBranch(Long id) {
        Branch branch = getById(id);

        // Nếu chi nhánh đã có phòng chiếu, xóa cứng sẽ làm đứt dữ liệu vận hành.
        // Chỗ này mình chặn lại để admin chuyển sang INACTIVE nếu muốn ngừng dùng.
        if (branch.getCinemaRooms() != null && !branch.getCinemaRooms().isEmpty()) {
            throw new IllegalArgumentException("Chi nhánh đã có phòng chiếu, không thể xóa. Hãy chuyển trạng thái sang INACTIVE.");
        }
        branchRepository.delete(branch);
    }

    private void validateUniqueName(String branchName, Long currentId) {
        String normalized = branchName == null ? "" : branchName.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Tên chi nhánh không được để trống.");
        }

        boolean duplicated = currentId == null
                ? branchRepository.existsByBranchNameIgnoreCase(normalized)
                : branchRepository.existsByBranchNameIgnoreCaseAndBranchIdNot(normalized, currentId);

        if (duplicated) {
            throw new IllegalArgumentException("Tên chi nhánh đã tồn tại.");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
