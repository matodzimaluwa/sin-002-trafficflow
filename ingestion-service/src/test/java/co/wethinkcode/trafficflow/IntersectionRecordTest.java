
package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntersectionRecordTest {

    @Test
    void storesIntersectionDetails() {
        IntersectionRecord record = new IntersectionRecord(
                "INT-1001", "Downtown", "4-way", true
        );

        assertEquals("INT-1001", record.id());
        assertEquals("Downtown", record.district());
        assertEquals("4-way", record.signalType());
        assertTrue(record.active());
    }
}
