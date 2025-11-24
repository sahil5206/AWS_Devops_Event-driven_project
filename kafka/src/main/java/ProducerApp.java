import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class ProducerApp {
    private static final String TOPIC = "events";
    private static final String BOOTSTRAP =
            System.getenv().getOrDefault("BOOTSTRAP_SERVERS", "kafka-broker.default.svc.cluster.local:9092");
    private static Producer<String, String> producer;
    private static final AtomicLong EVENT_COUNTER = new AtomicLong(0);

    public static void main(String[] args) throws IOException {
        // Kafka configuration
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        try {
            producer = new KafkaProducer<>(props);
        } catch (Exception e) {
            System.err.println("Failed to create producer with bootstrap servers: " + BOOTSTRAP);
            e.printStackTrace();
            throw e;
        }

        // HTTP API for sending events
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/send", ProducerApp::handleSendEvent);
        server.createContext("/metrics", ProducerApp::handleMetrics);
        server.start();

        System.out.println("🚀 ProducerApp running on port 8080 ...");
    }

    private static void handleSendEvent(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String msg = "Producer API is running. Send a POST with JSON payload to publish events.";
            exchange.sendResponseHeaders(200, msg.length());
            exchange.getResponseBody().write(msg.getBytes(StandardCharsets.UTF_8));
            exchange.close();
            return;
        } else if (!"POST".equals(exchange.getRequestMethod())) {
            String msg = "Method not allowed. Use POST with JSON payload.";
            exchange.sendResponseHeaders(405, msg.length());
            exchange.getResponseBody().write(msg.getBytes(StandardCharsets.UTF_8));
            exchange.close();
            return;
        }

        InputStream is = exchange.getRequestBody();
        String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // Basic JSON validation: must contain "eventType"
        if (!requestBody.contains("eventType")) {
            String resp = "❌ Missing eventType in JSON!";
            exchange.sendResponseHeaders(400, resp.length());
            exchange.getResponseBody().write(resp.getBytes());
            exchange.close();
            return;
        }

        // Send event to Kafka
        producer.send(new ProducerRecord<>(TOPIC, requestBody));
        EVENT_COUNTER.incrementAndGet();
        System.out.println("📤 Event sent: " + requestBody);

        String response = "✅ Event published successfully!";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    private static void handleMetrics(HttpExchange exchange) throws IOException {
        StringBuilder body = new StringBuilder();
        body.append("# HELP producer_events_total Total events published by ProducerApp\n");
        body.append("# TYPE producer_events_total counter\n");
        body.append("producer_events_total ").append(EVENT_COUNTER.get()).append("\n");

        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
