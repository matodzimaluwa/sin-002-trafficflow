
package co.wethinkcode.trafficflow;

import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IntersectionDataCleaner {

    public static List<IntersectionRecord> loadAndClean(String csvPath) {
        // Store intersections by ID to prevent duplicate records.
        Map<String, IntersectionRecord> intersections = new LinkedHashMap<>();

        try (InputStream input = IntersectionDataCleaner.class
                .getClassLoader().getResourceAsStream(csvPath)) {

            if (input == null) {
                throw new IllegalArgumentException("CSV file not found: " + csvPath);
            }

            try (CSVReader reader = new CSVReader(new InputStreamReader(input))) {
                reader.readNext(); // Skip the CSV header.

                String[] row;
                while ((row = reader.readNext()) != null) {
                    if (row.length < 4) {
                        continue;
                    }

                    // Clean whitespace and standardize the intersection ID.
                    String id = row[0].trim().toUpperCase();
                    String district = normalizeDistrict(row[1]);
                    String signalType = cleanValue(row[2]);

                    if (signalType != null) {
                        signalType = signalType.toLowerCase();
                    }

                    // Convert different boolean values into true or false.
                    String activeValue = row[3].trim();
                    boolean active = activeValue.equalsIgnoreCase("Y")
                            || activeValue.equalsIgnoreCase("YES")
                            || activeValue.equalsIgnoreCase("TRUE")
                            || activeValue.equals("1");

                    IntersectionRecord intersection = new IntersectionRecord(
                            id, district, signalType, active
                    );

                    // Keep the first record when an intersection ID appears more than once.
                    intersections.putIfAbsent(id, intersection);
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("Could not read intersections CSV", e);
        }

        // Return the cleaned records in their original order.
        return new ArrayList<>(intersections.values());
    }
    private static String cleanValue(String value) {
        String cleaned = value.trim().replaceAll("\\s+", " ");

        if (cleaned.isEmpty()
                || cleaned.matches("(?i)N/A|TBD|unknown|-|NaN")) {
            return null;
        }

        return cleaned;
    }

    private static String normalizeDistrict(String value) {
        String cleaned = cleanValue(value);

        if (cleaned == null) {
            return null;
        }

        String[] words = cleaned.toLowerCase().split(" ");
        StringBuilder district = new StringBuilder();

        for (String word : words) {
            if (district.length() > 0) {
                district.append(" ");
            }

            district.append(Character.toUpperCase(word.charAt(0)));
            district.append(word.substring(1));
        }

        return district.toString();
    }
}
