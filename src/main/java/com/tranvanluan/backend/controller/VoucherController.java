package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.entity.Voucher;
import com.tranvanluan.backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    /**
     * Lấy danh sách voucher UNUSED của user.
     * Frontend dùng để hiển thị trên trang chọn F&B.
     */
    @GetMapping("/user/{userId}")
    public List<Voucher> getUnusedVouchers(@PathVariable Long userId) {
        return voucherService.getUnusedVouchersByUser(userId);
    }
}
