package com.ecopickup;

import com.ecopickup.model.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EcoPickupApplicationTests {
    @Test
    void workflowStatusesCoverThePlannedPickupJourney() {
        assertThat(PickupStatus.values()).containsExactly(
                PickupStatus.REQUEST_SUBMITTED,
                PickupStatus.REQUEST_ACCEPTED,
                PickupStatus.PICKUP_SCHEDULED,
                PickupStatus.COLLECTOR_ASSIGNED,
                PickupStatus.PICKED_UP,
                PickupStatus.PROCESSING_COMPLETED
        );
        assertThat(UserType.values()).contains(UserType.SELLER, UserType.BUYER, UserType.BOTH, UserType.ADMIN);
    }
}
