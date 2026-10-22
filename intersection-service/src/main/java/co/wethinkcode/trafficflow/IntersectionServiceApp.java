package co.wethinkcode.trafficflow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class IntersectionServiceApp {

    private static final String INGESTION_URL = "http://localhost:7020/intersections";

    // Canonical list is fetched from ingestion-service on first use, then kept in memory.
    // It stays null until the first successful fetch.
    private static List<JsonObject> intersections;

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        // If ingestion-service can't be reached, getIntersections() throws an IOException.
        app.exception(IOException.class, (e, ctx) ->
                ctx.status(503).result("A required service is unreachable"));

        // Look up a single intersection by ID (case-insensitive). Used by routing-service.
        app.get("/intersections/{id}", ctx -> {
            String requestedId = ctx.pathParam("id").trim();

            for (JsonObject intersection : getIntersections()) {
                if (intersection.get("id").getAsString().equalsIgnoreCase(requestedId)) {
                    ctx.contentType("application/json").result(intersection.toString());
                    return;
                }
            }

            ctx.status(404).result("Intersection not found");
        });

        // Validate a district name and list the intersections that belong to it.
        app.get("/districts/{name}", ctx -> {
            String requestedName = ctx.pathParam("name").trim();
            String districtName = null;
            JsonArray ids = new JsonArray();

            for (JsonObject intersection : getIntersections()) {
                JsonElement district = intersection.get("district");

                // Some records have a null district (missing in the CSV), so check before reading it.
                if (district != null && !district.isJsonNull()
                        && district.getAsString().equalsIgnoreCase(requestedName)) {
                    // Keep the properly cased name from the data, not what the caller typed.
                    districtName = district.getAsString();
                    ids.add(intersection.get("id").getAsString());
                }
            }

            // No intersection used this district name, so it isn't a known district.
            if (districtName == null) {
                ctx.status(404).result("District not found");
                return;
            }

            JsonObject result = new JsonObject();
            result.addProperty("district", districtName);
            result.add("intersections", ids);
            ctx.contentType("application/json").result(result.toString());
        });
    }

    // Returns the cached list, fetching it from ingestion-service the first time only.
    private static synchronized List<JsonObject> getIntersections()
            throws IOException, InterruptedException {
        if (intersections == null) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(INGESTION_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Treat an error response like an unreachable service, so it also becomes a 503.
            if (response.statusCode() != 200) {
                throw new IOException("Ingestion service returned " + response.statusCode());
            }

            List<JsonObject> loaded = new ArrayList<>();
            for (JsonElement element : JsonParser.parseString(response.body()).getAsJsonArray()) {
                loaded.add(element.getAsJsonObject());
            }
            // Only assign after the whole list parsed successfully, so a failed
            // fetch never leaves a half-filled cache behind.
            intersections = loaded;
        }

        return intersections;
    }
}

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
