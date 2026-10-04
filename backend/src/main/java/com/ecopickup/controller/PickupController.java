package com.ecopickup.controller;

import com.ecopickup.dto.WorkflowDtos.*;
import com.ecopickup.model.*;
import com.ecopickup.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.NoSuchElementException;
import java.util.List;

@RestController @RequestMapping("/api/pickups")
public class PickupController {
    private final PickupRepository pickups; private final ItemRequestRepository requests; private final RewardRepository rewards;
    public PickupController(PickupRepository pickups,ItemRequestRepository requests,RewardRepository rewards){this.pickups=pickups;this.requests=requests;this.rewards=rewards;}
    @GetMapping public List<Pickup> all(){return pickups.findAll();}
    @GetMapping("/{id}") public Pickup one(@PathVariable String id){return find(id);}
    @GetMapping("/request/{requestId}") public Pickup byRequest(@PathVariable Long requestId){return pickups.findByRequestId(requestId).orElseThrow(()->new NoSuchElementException("Pickup not found"));}
    @GetMapping("/user/{userId}") public List<Pickup> byUser(@PathVariable Long userId){return pickups.findByRequestSellerIdOrRequestBuyerIdOrderByCreatedAtDesc(userId,userId);}

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
    public Pickup create(@Valid @RequestBody CreatePickupRequest input){
        ItemRequest request=requests.findById(input.requestId()).orElseThrow(()->new NoSuchElementException("Request not found"));
        if(request.getStatus()!=RequestStatus.ACCEPTED) throw new IllegalStateException("The item request must be accepted before scheduling pickup");
        if(pickups.findByRequestId(request.getId()).isPresent()) throw new IllegalStateException("A pickup is already scheduled for this request");
        Pickup pickup=new Pickup(); pickup.setId(nextId());pickup.setRequest(request);pickup.setAddress(input.address());pickup.setPickupDate(input.pickupDate());pickup.setTimeSlot(input.timeSlot());pickup.setContactNumber(input.contactNumber());pickup.setInstructions(input.instructions());
        request.setStatus(RequestStatus.PICKUP_SCHEDULED); requests.save(request);
        Pickup saved=pickups.save(pickup); Reward reward=new Reward();reward.setPickup(saved);reward.setEstimatedValue(request.getItem().getExpectedPrice());rewards.save(reward);return saved;
    }
    @PatchMapping("/{id}/status") @Transactional
    public Pickup updateStatus(@PathVariable String id,@Valid @RequestBody UpdatePickupStatus input){
        Pickup pickup=find(id);pickup.setStatus(input.status());if(input.collectorName()!=null&&!input.collectorName().isBlank())pickup.setCollectorName(input.collectorName());
        if(input.status()==PickupStatus.PROCESSING_COMPLETED){pickup.getRequest().setStatus(RequestStatus.COMPLETED);pickup.getRequest().getItem().setStatus(ItemStatus.COMPLETED);requests.save(pickup.getRequest());}
        return pickups.save(pickup);
    }
    @PostMapping("/{id}/handover") @Transactional
    public Pickup completeHandover(@PathVariable String id,@Valid @RequestBody CompleteHandoverRequest input){
        Pickup pickup=find(id);
        if(pickup.getStatus()!=PickupStatus.COLLECTOR_ASSIGNED) throw new IllegalStateException("Assign a collector before completing the handover");
        Reward reward=rewards.findByPickupId(pickup.getId()).orElseThrow(()->new NoSuchElementException("Reward record not found"));
        reward.setFinalApprovedValue(input.finalApprovedValue());reward.setPaymentMethod(input.paymentMethod());reward.setPaymentStatus(PaymentStatus.COMPLETED);rewards.save(reward);
        pickup.setStatus(PickupStatus.PICKED_UP);return pickups.save(pickup);
    }
    private Pickup find(String id){return pickups.findById(id.toUpperCase()).orElseThrow(()->new NoSuchElementException("Pickup not found"));}
    private String nextId(){int number=1025+(int)pickups.count();String id="EW"+number;while(pickups.existsById(id))id="EW"+(++number);return id;}
}
