package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CongestionServiceTest {

    @Test
    void acceptsLevelsFromZeroToEight() {
        assertTrue(CongestionServiceApp.isValidLevel(0));
        assertTrue(CongestionServiceApp.isValidLevel(8));
    }

    @Test
    void rejectsLevelsOutsideZeroToEight() {
        assertFalse(CongestionServiceApp.isValidLevel(-1));
        assertFalse(CongestionServiceApp.isValidLevel(9));
    }
}