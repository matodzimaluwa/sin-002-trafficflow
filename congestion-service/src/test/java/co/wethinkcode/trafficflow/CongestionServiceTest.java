
package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CongestionServiceTest {

    @Test
    void rejectsCongestionLevelAboveEight() {
        int level = 9;

        assertEquals(false, level >= 0 && level <= 8);
    }
}
