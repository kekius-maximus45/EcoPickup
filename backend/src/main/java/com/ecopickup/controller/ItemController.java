package com.ecopickup.controller;

import com.ecopickup.dto.ItemDtos.CreateItemRequest;
import com.ecopickup.model.*;
import com.ecopickup.repository.*;
import com.ecopickup.service.ItemImageStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/items")
public class ItemController {
    private final ItemRepository items; private final UserRepository users; private final ItemImageStorageService imageStorage;
    public ItemController(ItemRepository items,UserRepository users,ItemImageStorageService imageStorage){this.items=items;this.users=users;this.imageStorage=imageStorage;}

    @GetMapping
    public List<Item> browse(@RequestParam(required=false) String search,@RequestParam(required=false) String category,@RequestParam(required=false) ItemCondition condition){
        return items.search(blankToNull(search),blankToNull(category),condition);
    }
    @GetMapping("/{id}") public Item one(@PathVariable Long id){return find(id);}
    @GetMapping("/seller/{sellerId}") public List<Item> sellerItems(@PathVariable Long sellerId){return items.findBySellerIdOrderByCreatedAtDesc(sellerId);}
    @PostMapping(consumes=MediaType.APPLICATION_JSON_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public Item create(@Valid @RequestBody CreateItemRequest input){
        return save(input,input.imageUrl());
    }
    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @ResponseStatus(HttpStatus.CREATED)
    public Item createWithImage(@Valid @RequestPart("item") CreateItemRequest input,@RequestPart(value="image",required=false) MultipartFile image){
        return save(input,image==null||image.isEmpty()?null:imageStorage.store(image));
    }
    private Item save(CreateItemRequest input,String imageUrl){
        User seller=users.findById(input.sellerId()).orElseThrow(()->new NoSuchElementException("Seller not found"));
        if(seller.getUserType()==UserType.ADMIN) throw new IllegalArgumentException("Admin accounts cannot publish marketplace listings");
        Item item=new Item(); item.setSeller(seller);item.setCategory(input.category());item.setName(input.name());item.setBrand(input.brand());item.setCondition(input.condition());item.setQuantity(input.quantity());item.setDescription(input.description());item.setExpectedPrice(input.expectedPrice());item.setImageUrl(imageUrl);item.setLocation(input.location());
        return items.save(item);
    }
    private Item find(Long id){return items.findById(id).orElseThrow(()->new NoSuchElementException("Item not found"));}
    private String blankToNull(String value){return value==null||value.isBlank()?null:value;}
}
