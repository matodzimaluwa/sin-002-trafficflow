package co.wethinkcode.trafficflow;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;

public class RoutingServiceApp {

    public static void main(String[] args) {
        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        app.exception(IOException.class, (e, ctx) ->
                ctx.status(503).result("A required service is unreachable"));

        // TODO (Provides estimated travel times based on congestion and intersection.)
        // Add domain endpoints for routing-service here.
        app.get("/route/{id}", ctx -> {
            String id = ctx.pathParam("id");
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest intersectionRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7021/intersections/" + id))
                    .GET()
                    .build();

            HttpResponse<String> intersectionResponse = client.send(
                    intersectionRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (intersectionResponse.statusCode() == 404) {
                ctx.status(404).result("Intersection not found");
                return;
            }
            if (intersectionResponse.statusCode() != 200) {
                ctx.status(502).result("Intersection service unavailable");
                return;
            }

            HttpRequest congestionRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:7022/congestion"))
                    .GET()
                    .build();

            HttpResponse<String> congestionResponse = client.send(
                    congestionRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (congestionResponse.statusCode() != 200) {
                ctx.status(502).result("Congestion service unavailable");
                return;
            }

            JsonObject intersection = JsonParser.parseString(
                    intersectionResponse.body()
            ).getAsJsonObject();

            JsonObject congestion = JsonParser.parseString(
                    congestionResponse.body()
            ).getAsJsonObject();

            int level = congestion.get("level").getAsInt();
            int estimatedTravelTime = 5 + (level * 2);

            JsonObject result = new JsonObject();
            result.add("intersection", intersection);
            result.addProperty("congestionLevel", level);
            result.addProperty("estimatedTravelTimeMinutes", estimatedTravelTime);

            ctx.contentType("application/json").result(result.toString());
        });
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
