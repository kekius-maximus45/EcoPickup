package com.ecopickup.dto;

import com.ecopickup.model.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public final class ItemDtos {
    private ItemDtos() {}
    public record CreateItemRequest(@NotNull Long sellerId,@NotBlank String category,@NotBlank String name,String brand,@NotNull ItemCondition condition,@Min(1) int quantity,@NotBlank @Size(max=2000) String description,@NotNull @DecimalMin("0.0") BigDecimal expectedPrice,String imageUrl,@NotBlank String location){}
}
