package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutingServiceTest {

    @Test
    void travelTimeIsFiveMinutesWhenThereIsNoCongestion() {
        assertEquals(5, RoutingServiceApp.estimateTravelTime(0));
    }

    @Test
    void travelTimeGrowsTwoMinutesPerCongestionLevel() {
        assertEquals(15, RoutingServiceApp.estimateTravelTime(5));
        assertEquals(21, RoutingServiceApp.estimateTravelTime(8));
    }

    @Test
    void higherCongestionNeverGivesShorterTravelTime() {
        for (int level = 0; level < 8; level++) {
            assertTrue(RoutingServiceApp.estimateTravelTime(level + 1)
                    > RoutingServiceApp.estimateTravelTime(level));
        }
    }
}