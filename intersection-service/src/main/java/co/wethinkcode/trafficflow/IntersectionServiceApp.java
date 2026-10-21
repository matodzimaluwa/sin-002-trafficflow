package co.wethinkcode.trafficflow;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IntersectionServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        // TODO (Validates intersection/district names (source of truth).)
        // Add domain endpoints for intersection-service here.
        app.get("/intersections/{id}", ctx -> {
            String requestedId = ctx.pathParam("id").trim();

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7020/intersections"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                ctx.status(502).result("Ingestion service unavailable");
                return;
            }

            JsonArray intersections =
                    JsonParser.parseString(response.body()).getAsJsonArray();

            for (JsonElement element : intersections) {
                JsonObject intersection = element.getAsJsonObject();
                String id = intersection.get("id").getAsString();

                if (id.equalsIgnoreCase(requestedId)) {
                    ctx.contentType("application/json")
                            .result(intersection.toString());
                    return;
                }
            }

            ctx.status(404).result("Intersection not found");
        });
    }
}

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.
