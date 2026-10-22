package co.wethinkcode.trafficflow;

import io.javalin.json.JavalinGson;
import io.javalin.Javalin;

public class CongestionServiceApp {

    private static int congestionLevel = 0;

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> config.jsonMapper(new JavalinGson())).start(7022);
        app.get("/health", ctx -> ctx.result("OK"));

        // Add domain endpoints for congestion-service here.
        app.get("/congestion", ctx -> {
            ctx.json(java.util.Map.of("level", congestionLevel));
        });

        app.put("/congestion", ctx -> {
            String value = ctx.queryParam("level");

            if (value == null || value.isBlank()) {
                ctx.status(400).result("Congestion level is required");
                return;
            }

            int level;

            try {
                level = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                ctx.status(400).result("Congestion level must be a number");
                return;
            }

            if (!isValidLevel(level)) {
                ctx.status(400).result("Congestion level must be between 0 and 8");
                return;
            }

            congestionLevel = level;
            ctx.json(java.util.Map.of("level", congestionLevel));
        });
    }

    static boolean isValidLevel(int level) {
        return level >= 0 && level <= 8;
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
