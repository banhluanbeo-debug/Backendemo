package com.tranvanluan.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItemDTO {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String imageUrl;
    private Boolean isActive;
}
