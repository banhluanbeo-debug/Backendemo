package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.entity.OrderHistory;
import com.tranvanluan.backend.entity.User;
import com.tranvanluan.backend.repository.OrderHistoryRepository;
import com.tranvanluan.backend.repository.UserRepository;
import com.tranvanluan.backend.service.RewardService;
import com.tranvanluan.backend.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final OrderHistoryRepository orderHistoryRepository;
    private final UserRepository userRepository;
    private final VoucherService voucherService;

    @Override
    @Transactional
    public void checkAndGrantRewards(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("RewardService: User id={} không tồn tại", userId);
            return;
        }

        List<OrderHistory> histories = orderHistoryRepository.findByUserId(userId);

        long totalTickets = histories.stream()
                .filter(h -> "PAID".equalsIgnoreCase(h.getStatus()))
                .mapToLong(h -> countSeats(h.getSeatCodes()))
                .sum();

        log.info("RewardService: user={} totalTickets={} rewardLevel={}",
                userId, totalTickets, user.getRewardLevel());

        int currentLevel = user.getRewardLevel() == null ? 0 : user.getRewardLevel();
        boolean updated = false;

        if (totalTickets >= 10 && currentLevel < 10) {
            voucherService.createVoucher(userId, "FOOD50", 50000.0);
            user.setRewardLevel(10);
            currentLevel = 10;
            updated = true;
            log.info("RewardService: Tặng FOOD50 cho user={}", userId);
        }

        if (totalTickets >= 5 && currentLevel < 5) {
            voucherService.createVoucher(userId, "FOOD30", 30000.0);
            user.setRewardLevel(5);
            updated = true;
            log.info("RewardService: Tặng FOOD30 cho user={}", userId);
        }

        if (updated) {
            userRepository.save(user);
        }
    }

    
    private long countSeats(String seatCodes) {
        if (seatCodes == null || seatCodes.trim().isEmpty()) return 0;
        return seatCodes.split(",").length;
    }
}
