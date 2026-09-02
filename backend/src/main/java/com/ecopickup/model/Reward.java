package com.ecopickup.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity @Table(name = "rewards")
public class Reward {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) @JoinColumn(unique = true) private Pickup pickup;
    @Column(precision = 12, scale = 2) private BigDecimal estimatedValue;
    @Column(precision = 12, scale = 2) private BigDecimal finalApprovedValue;
    @Enumerated(EnumType.STRING) private PaymentMethod paymentMethod;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    public Reward() {}
    public Long getId(){return id;} public Pickup getPickup(){return pickup;} public void setPickup(Pickup v){pickup=v;}
    public BigDecimal getEstimatedValue(){return estimatedValue;} public void setEstimatedValue(BigDecimal v){estimatedValue=v;} public BigDecimal getFinalApprovedValue(){return finalApprovedValue;} public void setFinalApprovedValue(BigDecimal v){finalApprovedValue=v;}
    public PaymentMethod getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(PaymentMethod v){paymentMethod=v;} public PaymentStatus getPaymentStatus(){return paymentStatus;} public void setPaymentStatus(PaymentStatus v){paymentStatus=v;}
}
