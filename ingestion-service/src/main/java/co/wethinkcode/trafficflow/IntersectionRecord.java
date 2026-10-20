
package co.wethinkcode.trafficflow;

public record IntersectionRecord(
        String id,
        String district,
        String signalType,
        boolean active
) {
}
