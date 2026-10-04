package com.ecopickup.repository;
import com.ecopickup.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface ItemRepository extends JpaRepository<Item,Long>{
    @Query("""
        select i from Item i where (
            i.status = com.ecopickup.model.ItemStatus.ACTIVE
            or i.status = com.ecopickup.model.ItemStatus.REQUESTED
        )
        and (:search is null or lower(i.name) like lower(concat('%',:search,'%')) or lower(i.location) like lower(concat('%',:search,'%')))
        and (:category is null or i.category = :category)
        and (:condition is null or i.condition = :condition)
        order by i.createdAt desc
        """)
    List<Item> search(@Param("search") String search,@Param("category") String category,@Param("condition") ItemCondition condition);
    List<Item> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
}
