package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.Order;
import org.springframework.stereotype.Service;

@Service
public class VietQrService {

    public String generateQr(Order order) {
        return String.format(
    "https://img.vietqr.io/image/MB-0876064815-compact2.png?amount=%d&addInfo=ORDER_%d&accountName=TRAN%%20VAN%%20LUAN",
    order.getTotalAmount().longValue(),
    order.getId()
);
    }
}
