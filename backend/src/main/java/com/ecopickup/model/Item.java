package com.ecopickup.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "items")
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) private User seller;
    @Column(nullable = false) private String category;
    @Column(nullable = false) private String name;
    private String brand;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ItemCondition condition;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false, length = 2000) private String description;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal expectedPrice;
    private String imageUrl;
    @Column(nullable = false) private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ItemStatus status = ItemStatus.ACTIVE;
    @Column(nullable = false, updatable = false) private LocalDateTime createdAt = LocalDateTime.now();

    public Item() {}
    public Long getId(){return id;} public User getSeller(){return seller;} public void setSeller(User v){seller=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;} public String getName(){return name;} public void setName(String v){name=v;}
    public String getBrand(){return brand;} public void setBrand(String v){brand=v;} public ItemCondition getCondition(){return condition;} public void setCondition(ItemCondition v){condition=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public BigDecimal getExpectedPrice(){return expectedPrice;} public void setExpectedPrice(BigDecimal v){expectedPrice=v;} public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;} public ItemStatus getStatus(){return status;} public void setStatus(ItemStatus v){status=v;}
    public LocalDateTime getCreatedAt(){return createdAt;}
}
