package com.ecopickup.repository;
import com.ecopickup.model.Pickup;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface PickupRepository extends JpaRepository<Pickup,String>{
    Optional<Pickup> findByRequestId(Long requestId);
    List<Pickup> findByRequestSellerIdOrRequestBuyerIdOrderByCreatedAtDesc(Long sellerId,Long buyerId);
}
