package com.tranvanluan.backend.repository;

import com.tranvanluan.backend.entity.Voucher;
import com.tranvanluan.backend.entity.Voucher.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByUserId(Long userId);
    List<Voucher> findByUserIdAndStatus(Long userId, VoucherStatus status);
    Optional<Voucher> findByCode(String code);
}
