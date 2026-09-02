package com.ecopickup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "item_requests")
public class ItemRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Item item;
    @ManyToOne(optional = false) private User buyer;
    @ManyToOne(optional = false) private User seller;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private RequestStatus status = RequestStatus.PENDING;
    @Column(nullable = false, updatable = false) private LocalDateTime requestDate = LocalDateTime.now();

    public ItemRequest() {}
    public ItemRequest(Item item, User buyer, User seller){this.item=item;this.buyer=buyer;this.seller=seller;}
    public Long getId(){return id;} public Item getItem(){return item;} public User getBuyer(){return buyer;} public User getSeller(){return seller;}
    public RequestStatus getStatus(){return status;} public void setStatus(RequestStatus v){status=v;} public LocalDateTime getRequestDate(){return requestDate;}
}
