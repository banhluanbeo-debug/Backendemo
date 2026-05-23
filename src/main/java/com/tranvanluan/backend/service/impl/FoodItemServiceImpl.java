package com.tranvanluan.backend.service.impl;

import com.tranvanluan.backend.dto.FoodItemDTO;
import com.tranvanluan.backend.entity.FoodItem;
import com.tranvanluan.backend.repository.FoodItemRepository;
import com.tranvanluan.backend.service.FoodItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodItemServiceImpl implements FoodItemService {

    private final FoodItemRepository foodItemRepository;

    @Override
    public List<FoodItemDTO> getAll() {
        return foodItemRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoodItemDTO> getActive() {
        return foodItemRepository.findByIsActiveTrue().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FoodItemDTO getById(Long id) {
        FoodItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Food item not found: " + id));
        return mapToDTO(item);
    }

    @Override
    public FoodItemDTO create(FoodItemDTO dto) {
        FoodItem item = FoodItem.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return mapToDTO(foodItemRepository.save(item));
    }

    @Override
    public FoodItemDTO update(Long id, FoodItemDTO dto) {
        FoodItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Food item not found: " + id));
        
        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        item.setDescription(dto.getDescription());
        item.setImageUrl(dto.getImageUrl());
        if (dto.getIsActive() != null) {
            item.setIsActive(dto.getIsActive());
        }
        item.setUpdatedAt(LocalDateTime.now());
        
        return mapToDTO(foodItemRepository.save(item));
    }

    @Override
    public void delete(Long id) {
        FoodItem item = foodItemRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Food item not found: " + id));
        // Hard delete for now since OrderHistory stores snapshot (String of names)
        foodItemRepository.delete(item);
    }

    private FoodItemDTO mapToDTO(FoodItem item) {
        return FoodItemDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .description(item.getDescription())
                .imageUrl(item.getImageUrl())
                .isActive(item.getIsActive())
                .build();
    }
}
