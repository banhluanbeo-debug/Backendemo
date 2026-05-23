package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.Voucher;
import java.util.List;

public interface VoucherService {
    List<Voucher> getUnusedVouchersByUser(Long userId);
    void markUsed(String code);
    Voucher createVoucher(Long userId, String type, Double discountAmount);
}
