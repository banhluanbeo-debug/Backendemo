package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.dto.FoodItemDTO;
import com.tranvanluan.backend.service.FoodItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/foods")
@RequiredArgsConstructor
public class FoodItemController {

    private final FoodItemService foodItemService;

    // Admin views all
    @GetMapping
    public List<FoodItemDTO> getAll() {
        return foodItemService.getAll();
    }

    // User views active only
    @GetMapping("/active")
    public List<FoodItemDTO> getActive() {
        return foodItemService.getActive();
    }

    @GetMapping("/{id}")
    public FoodItemDTO getById(@PathVariable Long id) {
        return foodItemService.getById(id);
    }

    @PostMapping
    public FoodItemDTO create(@RequestBody FoodItemDTO dto) {
        return foodItemService.create(dto);
    }

    @PutMapping("/{id}")
    public FoodItemDTO update(@PathVariable Long id, @RequestBody FoodItemDTO dto) {
        return foodItemService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        foodItemService.delete(id);
    }
}
