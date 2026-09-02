package com.ecopickup.repository;
import com.ecopickup.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ItemRequestRepository extends JpaRepository<ItemRequest,Long>{
    List<ItemRequest> findBySellerIdOrderByRequestDateDesc(Long sellerId);
    List<ItemRequest> findByBuyerIdOrderByRequestDateDesc(Long buyerId);
    boolean existsByItemIdAndBuyerIdAndStatus(Long itemId,Long buyerId,RequestStatus status);
}
