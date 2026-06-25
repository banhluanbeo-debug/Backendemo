package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.service.OrderService;
import com.tranvanluan.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PaymentController — chỉ nhận request, delegate xuống PaymentService và OrderService.
 * KHÔNG chứa bất kỳ logic hash hay VNPay nào.
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @Value("${frontend.url}")
    private String frontendUrl;

    
    @GetMapping("/payment-web")
    public ResponseEntity<Map<String, String>> createPaymentUrl(
            @RequestParam long amount,
            @RequestParam String orderId,
            HttpServletRequest request) {

        if (amount <= 0 || orderId == null || orderId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Thiếu dữ liệu: amount hoặc orderId"));
        }

        String ipAddr = getClientIp(request);

        String payUrl = paymentService.createVNPayUrl(amount, orderId, ipAddr);

        Map<String, String> response = new HashMap<>();
        response.put("url", payUrl);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/vnpay-return-web")
    public void handleVNPayReturn(
            @RequestParam Map<String, String> params,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {

        String rawOrderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");

        String orderId = rawOrderId;
        if (orderId != null && orderId.contains("_")) {
            orderId = orderId.split("_")[0];
        }

        System.out.println("🌐 VNPay callback — orderId=" + orderId + " (raw=" + rawOrderId + "), code=" + responseCode);

        if ("24".equals(responseCode)) {
            response.sendRedirect(frontendUrl);
            return;
        }

        boolean isValid = paymentService.verifyVNPayReturn(params);

        if (!isValid || !"00".equals(responseCode)) {
            System.err.println("❌ VNPay verify failed — orderId=" + orderId + ", code=" + responseCode);
            response.sendRedirect(frontendUrl + "/payment-fail");
            return;
        }

        try {
            long orderIdLong = Long.parseLong(orderId);
            orderService.confirmOrder(orderIdLong, null); // null vì VNPay callback không có userId
            System.out.println("✅ Order " + orderId + " confirmed successfully");
            response.sendRedirect(frontendUrl + "/payment-success");
        } catch (Exception e) {
            System.err.println("❌ Confirm order failed: " + e.getMessage());
            response.sendRedirect(frontendUrl + "/payment-success");
        }
    }

    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For có thể là list "ip1, ip2, ..." → lấy ip đầu tiên
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return (ip != null && !ip.isBlank()) ? ip : "127.0.0.1";
    }
}
