package com.tranvanluan.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Utility class xử lý tất cả logic thô của VNPay:
 * - HMAC SHA512 hashing
 * - Sort & encode params
 * - Build payment URL
 */
@Component
public class VNPayUtil {

    @Value("${vnp.tmnCode}")
    private String tmnCode;

    @Value("${vnp.hashSecret}")
    private String hashSecret;

    @Value("${vnp.payUrl}")
    private String payUrl;

    @Value("${vnp.returnUrl}")
    private String returnUrl;

    // ================================================================
    // Build Payment URL
    // ================================================================

    public String buildPaymentUrl(long amount, String orderId, String ipAddr) {
        Map<String, String> params = new TreeMap<>(); // TreeMap tự sort theo key

        params.put("vnp_Version", "2.1.1");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", Long.toString(amount * 100L));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddr != null ? ipAddr : "127.0.0.1");
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        params.put("vnp_CreateDate", now.format(fmt));
        params.put(
                "vnp_ExpireDate",
                now.plusMinutes(15).format(fmt));
        System.out.println("TMN = " + tmnCode);
        System.out.println("SECRET = " + hashSecret);
        System.out.println("RETURN_URL = " + returnUrl);
        // Build raw hash string (params đã sort vì dùng TreeMap)
        String rawHash = buildRawString(params);
        String secureHash = hmacSHA512(hashSecret, rawHash);
        System.out.println("===== VNPAY DEBUG =====");
        System.out.println("TMN = [" + tmnCode + "]");
        System.out.println("SECRET LENGTH = " + (hashSecret == null ? 0 : hashSecret.length()));
        System.out.println("RETURN_URL = [" + returnUrl + "]");
        System.out.println("=======================");
        // Build query string (có encode)
        String queryString = buildQueryString(params);
        System.out.println("RAW HASH = " + rawHash);
        System.out.println("HASH = " + secureHash);
        return payUrl + "?"
                + queryString
                + "&vnp_SecureHashType=HmacSHA512"
                + "&vnp_SecureHash=" + secureHash;
    }

    // ================================================================
    // Verify Return URL signature
    // ================================================================

    public boolean verifyReturnUrl(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null)
            return false;

        // Tạo map mới bỏ hash fields
        Map<String, String> checkParams = new TreeMap<>(params);
        checkParams.remove("vnp_SecureHash");
        checkParams.remove("vnp_SecureHashType");

        String rawHash = buildRawString(checkParams);
        String calculatedHash = hmacSHA512(hashSecret, rawHash);

        return calculatedHash.equalsIgnoreCase(receivedHash);
    }

    // ================================================================
    // Private helpers
    // ================================================================

    /**
     * Build raw string để sign: key=value&key=value (không encode, đã sort)
     */
    private String buildRawString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {

                if (sb.length() > 0) {
                    sb.append("&");
                }

                sb.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue());
            }
        }

        return sb.toString();
    }

    /**
     * Build query string để append vào URL (có URL encode value)
     */
    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {

                if (sb.length() > 0)
                    sb.append("&");

                sb.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        }

        return sb.toString();
    }

    /**
     * HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi HMAC SHA512: " + e.getMessage(), e);
        }
    }
}
