package com.tranvanluan.backend.service;

import com.tranvanluan.backend.config.VNPayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * PaymentService — chứa toàn bộ business logic của thanh toán:
 * - Build VNPay URL
 * - Verify signature từ VNPay callback
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final VNPayUtil vnPayUtil;

    /**
     * Tạo VNPay payment URL
     *
     * @param amount  số tiền (đơn vị VND, ví dụ: 150000)
     * @param orderId ID của đơn hàng
     * @param ipAddr  IP của người dùng
     * @return VNPay payment URL để redirect
     */
    public String createVNPayUrl(long amount, String orderId, String ipAddr) {
        return vnPayUtil.buildPaymentUrl(amount, orderId, ipAddr);
    }

    /**
     * Verify chữ ký VNPay từ return URL params
     *
     * @param params toàn bộ query params từ VNPay callback
     * @return true nếu signature hợp lệ
     */
    public boolean verifyVNPayReturn(Map<String, String> params) {
        return vnPayUtil.verifyReturnUrl(params);
    }
}
