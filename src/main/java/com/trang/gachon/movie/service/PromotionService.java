package com.trang.gachon.movie.service;

import com.trang.gachon.movie.entity.Promotion;
import com.trang.gachon.movie.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    // ---- Public: chỉ lấy KM còn hiệu lực ----
    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now());
    }

    // ---- Admin: lấy tất cả ----
    public List<Promotion> getAll() {
        return promotionRepository.findAll();
    }

    public Promotion getById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khuyến mãi!"));
    }

    @Transactional
    public void save(Promotion promotion, MultipartFile imageFile) throws IOException {
        // Với edit, nếu không upload ảnh mới thì giữ banner cũ.
        if (promotion.getPromotionId() != null) {
            Promotion existing = getById(promotion.getPromotionId());
            if (promotion.getImage() == null || promotion.getImage().isBlank()) {
                promotion.setImage(existing.getImage());
            }
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            promotion.setImage(saveImage(imageFile));
        }
        promotionRepository.save(promotion);
    }

    @Transactional
    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }

    private String saveImage(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadDir = Paths.get("uploads");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Files.copy(file.getInputStream(),
                uploadDir.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }
}
