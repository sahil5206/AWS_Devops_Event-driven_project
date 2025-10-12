import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class ProducerApp {
    private static final String TOPIC = "events";
    private static Producer<String, String> producer;

    public static void main(String[] args) throws IOException {
        // Kafka configuration
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(props);

        // HTTP API for sending events
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/send", ProducerApp::handleSendEvent);
        server.start();

        System.out.println("🚀 ProducerApp running on port 8080 ...");
    }

    private static void handleSendEvent(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
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
        System.out.println("📤 Event sent: " + requestBody);

        String response = "✅ Event published successfully!";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
