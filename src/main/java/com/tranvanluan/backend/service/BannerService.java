package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.Banner;
import com.tranvanluan.backend.repository.BannerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<Banner> getAllBanners() {
        return bannerRepository.findAll();
    }

    public List<Banner> getActiveBanners() {
        return bannerRepository.findByIsActiveTrue();
    }

    public Banner getBannerById(Long id) {
        return bannerRepository.findById(id).orElse(null);
    }

    public Banner createBanner(Banner banner) {
        return bannerRepository.save(banner);
    }

    public Banner updateBanner(Long id, Banner bannerDetails) {
        Optional<Banner> optionalBanner = bannerRepository.findById(id);
        if (optionalBanner.isPresent()) {
            Banner existingBanner = optionalBanner.get();
            existingBanner.setTitle(bannerDetails.getTitle());
            existingBanner.setImageUrl(bannerDetails.getImageUrl());
            existingBanner.setLink(bannerDetails.getLink());
            existingBanner.setActive(bannerDetails.isActive());
            return bannerRepository.save(existingBanner);
        }
        return null;
    }

    public void deleteBanner(Long id) {
        bannerRepository.deleteById(id);
    }
}
