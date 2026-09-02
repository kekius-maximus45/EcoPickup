package com.ecopickup.config;

import com.ecopickup.model.*;
import com.ecopickup.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;

@Configuration
public class DemoDataConfig {
    @Bean
    CommandLineRunner seedDemoData(UserRepository users,ItemRepository items,PasswordEncoder encoder){
        return args->{
            if(users.count()>0)return;
            User seller=users.save(new User("Aarav Mehta","aarav@example.com","9876543210",encoder.encode("password"),UserType.SELLER,"Mumbai"));
            users.save(new User("GreenLoop Recyclers","buyer@greenloop.in","9123456780",encoder.encode("password"),UserType.BUYER,"Mumbai"));
            users.save(new User("EcoPickup Admin","admin@ecopickup.in","9000000000",encoder.encode("password"),UserType.ADMIN,"Mumbai"));
            add(items,seller,"Computers","Dell Inspiron Laptop","Dell",ItemCondition.WORKING,1,"Well-maintained laptop with charger. Battery backup is approximately two hours.","4000","Mumbai");
            add(items,seller,"Mobile","Samsung Galaxy Phone","Samsung",ItemCondition.PARTIALLY_WORKING,1,"Display is in good condition. Charging port needs repair.","1500","Pune");
            add(items,seller,"Printers","HP Inkjet Printer","HP",ItemCondition.NOT_WORKING,1,"Printer powers on but does not pull paper.","800","Nashik");
            add(items,seller,"TV","LG 32-inch LED TV","LG",ItemCondition.PARTIALLY_WORKING,1,"Sound works correctly but the screen flickers occasionally.","2200","Thane");
            add(items,seller,"Accessories","Mixed Copper Cables","Mixed",ItemCondition.NOT_WORKING,12,"Bundle of old charging, network and power cables for recycling.","650","Mumbai");
            add(items,seller,"Appliances","Whirlpool Microwave","Whirlpool",ItemCondition.WORKING,1,"Working microwave, cleaned and ready for reuse.","2800","Pune");
        };
    }
    private void add(ItemRepository items,User seller,String category,String name,String brand,ItemCondition condition,int quantity,String description,String price,String location){
        Item i=new Item();i.setSeller(seller);i.setCategory(category);i.setName(name);i.setBrand(brand);i.setCondition(condition);i.setQuantity(quantity);i.setDescription(description);i.setExpectedPrice(new BigDecimal(price));i.setLocation(location);items.save(i);
    }
}
