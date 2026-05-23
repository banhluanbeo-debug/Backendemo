package com.tranvanluan.backend.service;

public interface RewardService {
    /**
     * Kiểm tra tổng vé từ OrderHistory của user,
     * tặng voucher nếu đủ mốc và chưa nhận.
     */
    void checkAndGrantRewards(Long userId);
}
