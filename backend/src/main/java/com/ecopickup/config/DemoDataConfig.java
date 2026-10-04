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
            if(users.count()==0){
                User seller=users.save(new User("Aarav Mehta","aarav@example.com","9876543210",encoder.encode("password"),UserType.SELLER,"Mumbai"));
                users.save(new User("GreenLoop Recyclers","buyer@greenloop.in","9123456780",encoder.encode("password"),UserType.BUYER,"Mumbai"));
                users.save(new User("EcoPickup Admin","admin@ecopickup.in","9000000000",encoder.encode("password"),UserType.ADMIN,"Mumbai"));
                add(items,seller,"Computers","Dell Inspiron Laptop","Dell",ItemCondition.WORKING,1,"Well-maintained laptop with charger. Battery backup is approximately two hours.","4000","Mumbai");
                add(items,seller,"Mobile","Samsung Galaxy Phone","Samsung",ItemCondition.PARTIALLY_WORKING,1,"Display is in good condition. Charging port needs repair.","1500","Pune");
                add(items,seller,"Printers","HP Inkjet Printer","HP",ItemCondition.NOT_WORKING,1,"Printer powers on but does not pull paper.","800","Nashik");
                add(items,seller,"TV","LG 32-inch LED TV","LG",ItemCondition.PARTIALLY_WORKING,1,"Sound works correctly but the screen flickers occasionally.","2200","Thane");
                add(items,seller,"Accessories","Mixed Copper Cables","Mixed",ItemCondition.NOT_WORKING,12,"Bundle of old charging, network and power cables for recycling.","650","Mumbai");
                add(items,seller,"Appliances","Whirlpool Microwave","Whirlpool",ItemCondition.WORKING,1,"Working microwave, cleaned and ready for reuse.","2800","Pune");
            }

            users.findByEmailIgnoreCase("tejasmore452@gmail.com").ifPresent(seller->{
                if(!items.findBySellerIdOrderByCreatedAtDesc(seller.getId()).isEmpty())return;
                add(items,seller,"Computers","Lenovo ThinkPad Laptop","Lenovo",ItemCondition.WORKING,1,"Reliable business laptop with charger, 8 GB RAM and a clean display.","8500","Mumbai");
                add(items,seller,"Mobile","OnePlus Nord Smartphone","OnePlus",ItemCondition.PARTIALLY_WORKING,1,"Phone works well but the battery drains faster than normal.","4200","Mumbai");
                add(items,seller,"TV","Sony 24-inch LED Monitor","Sony",ItemCondition.WORKING,1,"Full-HD monitor with HDMI cable and minor marks on the stand.","3200","Thane");
                add(items,seller,"Printers","Canon Laser Printer","Canon",ItemCondition.PARTIALLY_WORKING,1,"Print quality is good; the paper tray needs adjustment.","1800","Mumbai");
                add(items,seller,"Accessories","Logitech Keyboard and Mouse","Logitech",ItemCondition.WORKING,2,"Wired keyboard and optical mouse set, tested and working.","700","Navi Mumbai");
                add(items,seller,"Accessories","Wi-Fi Router and Adapters","TP-Link",ItemCondition.WORKING,3,"Dual-band router supplied with power adapter and two spare network adapters.","1200","Mumbai");
                add(items,seller,"Appliances","Philips Mixer Grinder","Philips",ItemCondition.PARTIALLY_WORKING,1,"Motor runs correctly; one jar coupling needs replacement.","950","Mumbai");
                add(items,seller,"Scrap","Desktop Computer Parts Bundle","Mixed",ItemCondition.NOT_WORKING,8,"Motherboards, RAM sticks, power supplies and cooling fans for parts or recycling.","1600","Kalyan");
                add(items,seller,"Accessories","Bluetooth Speaker","JBL",ItemCondition.PARTIALLY_WORKING,1,"Speaker output is clear, but the charging socket is loose.","900","Mumbai");
                add(items,seller,"Other","UPS and Backup Battery","APC",ItemCondition.NOT_WORKING,1,"Old UPS with battery included, suitable for repair or responsible recycling.","1100","Thane");
            });
        };
    }
    private void add(ItemRepository items,User seller,String category,String name,String brand,ItemCondition condition,int quantity,String description,String price,String location){
        Item i=new Item();i.setSeller(seller);i.setCategory(category);i.setName(name);i.setBrand(brand);i.setCondition(condition);i.setQuantity(quantity);i.setDescription(description);i.setExpectedPrice(new BigDecimal(price));i.setLocation(location);items.save(i);
    }
}
