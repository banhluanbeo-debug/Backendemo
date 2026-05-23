package com.tranvanluan.backend.service;

import com.tranvanluan.backend.dto.FoodItemDTO;

import java.util.List;

public interface FoodItemService {
    List<FoodItemDTO> getAll();
    List<FoodItemDTO> getActive();
    FoodItemDTO getById(Long id);
    FoodItemDTO create(FoodItemDTO dto);
    FoodItemDTO update(Long id, FoodItemDTO dto);
    void delete(Long id);
}
