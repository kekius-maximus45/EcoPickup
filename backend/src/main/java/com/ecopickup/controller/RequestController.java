package com.ecopickup.controller;

import com.ecopickup.dto.WorkflowDtos.*;
import com.ecopickup.model.*;
import com.ecopickup.repository.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/requests")
public class RequestController {
    private final ItemRequestRepository requests; private final ItemRepository items; private final UserRepository users;
    public RequestController(ItemRequestRepository requests,ItemRepository items,UserRepository users){this.requests=requests;this.items=items;this.users=users;}
    @GetMapping("/seller/{id}") public List<ItemRequest> seller(@PathVariable Long id){return requests.findBySellerIdOrderByRequestDateDesc(id);}
    @GetMapping("/buyer/{id}") public List<ItemRequest> buyer(@PathVariable Long id){return requests.findByBuyerIdOrderByRequestDateDesc(id);}
    @GetMapping("/{id}") public ItemRequest one(@PathVariable Long id){return find(id);}

    @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
    public ItemRequest create(@Valid @RequestBody CreateInterestRequest input){
        Item item=items.findById(input.itemId()).orElseThrow(()->new NoSuchElementException("Item not found"));
        User buyer=users.findById(input.buyerId()).orElseThrow(()->new NoSuchElementException("Buyer not found"));
        if(buyer.getUserType()==UserType.ADMIN) throw new IllegalArgumentException("Admin accounts cannot send item requests");
        if(Objects.equals(item.getSeller().getId(),buyer.getId())) throw new IllegalArgumentException("You cannot request your own item");
        if(item.getStatus()!=ItemStatus.ACTIVE&&item.getStatus()!=ItemStatus.REQUESTED) throw new IllegalStateException("This item is no longer available");
        if(requests.existsByItemIdAndBuyerIdAndStatus(item.getId(),buyer.getId(),RequestStatus.PENDING)) throw new IllegalStateException("You already sent a request for this item");
        item.setStatus(ItemStatus.REQUESTED); items.save(item);
        return requests.save(new ItemRequest(item,buyer,item.getSeller()));
    }
    @PatchMapping("/{id}/status") @Transactional
    public ItemRequest updateStatus(@PathVariable Long id,@Valid @RequestBody UpdateRequestStatus input){
        ItemRequest request=find(id); request.setStatus(input.status());
        if(input.status()==RequestStatus.ACCEPTED) request.getItem().setStatus(ItemStatus.RESERVED);
        if(input.status()==RequestStatus.REJECTED) request.getItem().setStatus(ItemStatus.ACTIVE);
        if(input.status()==RequestStatus.COMPLETED) request.getItem().setStatus(ItemStatus.COMPLETED);
        return requests.save(request);
    }
    private ItemRequest find(Long id){return requests.findById(id).orElseThrow(()->new NoSuchElementException("Request not found"));}
}
