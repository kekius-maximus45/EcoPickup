package com.ecopickup.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name = "pickups")
public class Pickup {
    @Id private String id;
    @OneToOne(optional = false) @JoinColumn(unique = true) private ItemRequest request;
    @Column(nullable = false, length = 500) private String address;
    @Column(nullable = false) private LocalDate pickupDate;
    @Column(nullable = false) private String timeSlot;
    @Column(nullable = false) private String contactNumber;
    @Column(length = 1000) private String instructions;
    private String collectorName;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PickupStatus status = PickupStatus.PICKUP_SCHEDULED;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();

    public Pickup() {}
    public String getId(){return id;} public void setId(String v){id=v;} public ItemRequest getRequest(){return request;} public void setRequest(ItemRequest v){request=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;} public LocalDate getPickupDate(){return pickupDate;} public void setPickupDate(LocalDate v){pickupDate=v;}
    public String getTimeSlot(){return timeSlot;} public void setTimeSlot(String v){timeSlot=v;} public String getContactNumber(){return contactNumber;} public void setContactNumber(String v){contactNumber=v;}
    public String getInstructions(){return instructions;} public void setInstructions(String v){instructions=v;} public String getCollectorName(){return collectorName;} public void setCollectorName(String v){collectorName=v;}
    public PickupStatus getStatus(){return status;} public void setStatus(PickupStatus v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
