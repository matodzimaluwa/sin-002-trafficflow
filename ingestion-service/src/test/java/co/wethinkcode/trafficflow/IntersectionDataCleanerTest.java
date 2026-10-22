
package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class IntersectionDataCleanerTest {

    @Test
    void loadsIntersectionsFromCsv() {
        List<IntersectionRecord> intersections =
                IntersectionDataCleaner.loadAndClean("intersections-legacy.csv");

        // Check that the CSV was loaded and produced records.
        assertFalse(intersections.isEmpty());
    }

    @Test
    void cleansIntersectionIdAndDistrictWhitespace() {
        List<IntersectionRecord> intersections =
                IntersectionDataCleaner.loadAndClean("intersections-legacy.csv");

        // IDs should be uppercase and district names should have extra spaces removed.
        assertEquals("INT-1001", intersections.get(0).id());
        assertEquals("Downtown", intersections.get(0).district());
    }

    @Test
    void standardizesSignalTypeCasing() {
        List<IntersectionRecord> intersections =
                IntersectionDataCleaner.loadAndClean("intersections-legacy.csv");

        // Signal types should use a consistent lowercase format.
        assertEquals("4-way", intersections.get(0).signalType());
        assertEquals("roundabout", intersections.get(4).signalType());
        assertEquals("4-way", intersections.get(5).signalType());
    }

    @Test
    void convertsDifferentBooleanValues() {
        List<IntersectionRecord> intersections =
                IntersectionDataCleaner.loadAndClean("intersections-legacy.csv");

        // Y and yes mean true; 0 and no mean false.
        assertEquals(true, intersections.get(0).active());
        assertEquals(true, intersections.get(1).active());
        assertEquals(false, intersections.get(2).active());
        assertEquals(true, intersections.get(3).active());
        assertEquals(true, intersections.get(4).active());
        assertEquals(false, intersections.get(5).active());
        assertEquals(true, intersections.get(6).active());
        assertEquals(false, intersections.get(7).active());
    }

    @Test
    void removesDuplicateIntersectionIds() {
        List<IntersectionRecord> intersections =
                IntersectionDataCleaner.loadAndClean("intersections-legacy.csv");

        // INT-1005 appears twice with different casing in the original CSV.
        long duplicateCount = intersections.stream()
                .filter(intersection -> intersection.id().equals("INT-1005"))
                .count();

        assertEquals(1, duplicateCount);
    }
    @Test
    void normalizesDistrictCasing() {
        List<IntersectionRecord> intersections =
                IntersectionDataCleaner.loadAndClean("intersections-legacy.csv");

        assertEquals("Downtown", intersections.get(2).district());
        assertEquals("Uptown", intersections.get(7).district());
        assertEquals("Midtown", intersections.get(10).district());
    }

    @Test
    void convertsMissingAndPlaceholderValuesToNull() {
        List<IntersectionRecord> intersections =
                IntersectionDataCleaner.loadAndClean("intersections-legacy.csv");

        assertNull(intersections.get(3).signalType());
        assertNull(intersections.get(6).signalType());
        assertNull(intersections.get(12).signalType());
        assertNull(intersections.get(14).district());
    }
    @Test
    void convertsUnknownActiveFlagToNull() {
        List<IntersectionRecord> intersections =
                IntersectionDataCleaner.loadAndClean("intersections-legacy.csv");

        // INT-1013 has "unknown" as its active flag.
        assertNull(intersections.get(12).active());
    }
}
