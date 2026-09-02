package com.ecopickup.repository;
import com.ecopickup.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RewardRepository extends JpaRepository<Reward,Long>{Optional<Reward> findByPickupId(String pickupId);}
