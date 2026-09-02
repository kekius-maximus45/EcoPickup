package com.ecopickup.controller;

import com.ecopickup.model.*;
import com.ecopickup.repository.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/dashboard")
public class DashboardController {
    private final UserRepository users; private final ItemRepository items; private final ItemRequestRepository requests; private final PickupRepository pickups;
    public DashboardController(UserRepository users,ItemRepository items,ItemRequestRepository requests,PickupRepository pickups){this.users=users;this.items=items;this.requests=requests;this.pickups=pickups;}
    @GetMapping("/admin")
    public Map<String,Long> admin(){
        long completed=pickups.findAll().stream().filter(p->p.getStatus()==PickupStatus.PROCESSING_COMPLETED).count();
        long pending=requests.findAll().stream().filter(r->r.getStatus()==RequestStatus.PENDING).count();
        return Map.of("totalUsers",users.count(),"totalListings",items.count(),"pendingRequests",pending,"completedPickups",completed);
    }
}
