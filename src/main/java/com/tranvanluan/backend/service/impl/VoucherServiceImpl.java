package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.entity.Voucher;
import com.tranvanluan.backend.entity.Voucher.VoucherStatus;
import com.tranvanluan.backend.repository.VoucherRepository;
import com.tranvanluan.backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> getUnusedVouchersByUser(Long userId) {
        return voucherRepository.findByUserIdAndStatus(userId, VoucherStatus.UNUSED);
    }

    @Override
    @Transactional
    public void markUsed(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại: " + code));

        if (voucher.getStatus() == VoucherStatus.USED) {
            throw new RuntimeException("Voucher đã được sử dụng: " + code);
        }

        voucher.setStatus(VoucherStatus.USED);
        voucher.setUsedAt(LocalDateTime.now());
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher createVoucher(Long userId, String type, Double discountAmount) {
        // Tạo code unique: FOOD30-XXXX hoặc FOOD50-XXXX
        String code = type + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        Voucher voucher = Voucher.builder()
                .code(code)
                .type(type)
                .discountAmount(discountAmount)
                .status(VoucherStatus.UNUSED)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        return voucherRepository.save(voucher);
    }
}
