package com.ecopickup.controller;

import com.ecopickup.dto.WorkflowDtos.UpdateRewardRequest;
import com.ecopickup.model.Reward;
import com.ecopickup.repository.RewardRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;

@RestController @RequestMapping("/api/rewards")
public class RewardController {
    private final RewardRepository rewards;
    public RewardController(RewardRepository rewards){this.rewards=rewards;}
    @GetMapping("/pickup/{pickupId}") public Reward get(@PathVariable String pickupId){return find(pickupId);}
    @PutMapping("/pickup/{pickupId}") public Reward update(@PathVariable String pickupId,@Valid @RequestBody UpdateRewardRequest input){Reward r=find(pickupId);r.setFinalApprovedValue(input.finalApprovedValue());r.setPaymentMethod(input.paymentMethod());r.setPaymentStatus(input.paymentStatus());return rewards.save(r);}
    private Reward find(String id){return rewards.findByPickupId(id.toUpperCase()).orElseThrow(()->new NoSuchElementException("Reward record not found"));}
}
