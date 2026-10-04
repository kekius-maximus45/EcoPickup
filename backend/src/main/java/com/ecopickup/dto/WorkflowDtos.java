package com.ecopickup.dto;

import com.ecopickup.model.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class WorkflowDtos {
    private WorkflowDtos() {}
    public record CreateInterestRequest(@NotNull Long itemId,@NotNull Long buyerId){}
    public record UpdateRequestStatus(@NotNull RequestStatus status){}
    public record CreatePickupRequest(@NotNull Long requestId,@NotBlank String address,@NotNull @FutureOrPresent LocalDate pickupDate,@NotBlank String timeSlot,@NotBlank String contactNumber,String instructions){}
    public record UpdatePickupStatus(@NotNull PickupStatus status,String collectorName){}
    public record UpdateRewardRequest(@PositiveOrZero BigDecimal finalApprovedValue,PaymentMethod paymentMethod,@NotNull PaymentStatus paymentStatus){}
    public record CompleteHandoverRequest(@NotNull @PositiveOrZero BigDecimal finalApprovedValue,@NotNull PaymentMethod paymentMethod){}
}
