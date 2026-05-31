package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.entity.Banner;
import com.tranvanluan.backend.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Banner>> getActiveBanners() {
        return ResponseEntity.ok(bannerService.getActiveBanners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable Long id) {
        Banner banner = bannerService.getBannerById(id);
        if (banner != null) {
            return ResponseEntity.ok(banner);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Banner> createBanner(@RequestBody Banner banner) {
        return ResponseEntity.ok(bannerService.createBanner(banner));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        Banner updatedBanner = bannerService.updateBanner(id, banner);
        if (updatedBanner != null) {
            return ResponseEntity.ok(updatedBanner);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }
}
