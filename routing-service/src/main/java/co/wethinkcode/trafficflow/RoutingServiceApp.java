package co.wethinkcode.trafficflow;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class RoutingServiceApp {

    private static final String INTERSECTION_URL = "http://localhost:7021/intersections/";
    private static final String CONGESTION_URL = "http://localhost:7022/congestion";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        // If a service we call can't be reached, client.send throws an IOException.
        app.exception(IOException.class, (e, ctx) ->
                ctx.status(503).result("A required service is unreachable"));

        // Estimate travel time between two intersections: GET /route?from=INT-1001&to=INT-1002
        app.get("/route", ctx -> {
            String from = ctx.queryParam("from");
            String to = ctx.queryParam("to");

            if (from == null || from.isBlank() || to == null || to.isBlank()) {
                ctx.status(400).result("Both 'from' and 'to' intersection IDs are required");
                return;
            }

            // findIntersection sets the error status itself when it returns null
            JsonObject fromIntersection = findIntersection(ctx, from);
            if (fromIntersection == null) {
                return;
            }
            JsonObject toIntersection = findIntersection(ctx, to);
            if (toIntersection == null) {
                return;
            }

            HttpResponse<String> congestionResponse = get(CONGESTION_URL);
            if (congestionResponse.statusCode() != 200) {
                ctx.status(502).result("Congestion service unavailable");
                return;
            }

            int level = JsonParser.parseString(congestionResponse.body())
                    .getAsJsonObject().get("level").getAsInt();

            int estimatedTravelTime = estimateTravelTime(level);

            JsonObject result = new JsonObject();
            result.add("from", fromIntersection);
            result.add("to", toIntersection);
            result.addProperty("congestionLevel", level);
            result.addProperty("estimatedTravelTimeMinutes", estimatedTravelTime);

            ctx.contentType("application/json").result(result.toString());
        });
    }

    static int estimateTravelTime(int congestionLevel) {
        return 5 + (congestionLevel * 2);
    }

    // null means ctx already has the 404/502
    private static JsonObject findIntersection(Context ctx, String id)
            throws IOException, InterruptedException {
        // Encode the ID so characters like spaces can't break the URL.
        HttpResponse<String> response = get(INTERSECTION_URL
                + URLEncoder.encode(id.trim(), StandardCharsets.UTF_8));

        if (response.statusCode() == 404) {
            ctx.status(404).result("Intersection not found: " + id);
            return null;
        }
        if (response.statusCode() != 200) {
            ctx.status(502).result("Intersection service unavailable");
            return null;
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    private static HttpResponse<String> get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
