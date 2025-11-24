import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class ConsumerApp {
    private static final LinkedList<String> lastEvents = new LinkedList<>();
    private static final int MAX_EVENTS = 50;
    private static final String BOOTSTRAP =
            System.getenv().getOrDefault("BOOTSTRAP_SERVERS", "kafka-broker.default.svc.cluster.local:9092");

    public static void main(String[] args) throws IOException {
        // Kafka consumer setup
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "event-consumer-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer;
        try {
            consumer = new KafkaConsumer<>(props);
        } catch (Exception e) {
            System.err.println("Failed to create consumer with bootstrap servers: " + BOOTSTRAP);
            e.printStackTrace();
            throw e;
        }
        consumer.subscribe(Collections.singletonList("events"));

        // HTTP server to serve last 50 events
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/events", ConsumerApp::handleGetEvents);
        server.start();
        System.out.println("👂 ConsumerApp running on port 8081 ...");

        // Consume events continuously
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                String msg = record.value();
                System.out.println("📥 Received: " + msg);
                synchronized (lastEvents) {
                    lastEvents.addFirst(msg);
                    if (lastEvents.size() > MAX_EVENTS) lastEvents.removeLast();
                }
            }
        }
    }

    private static void handleGetEvents(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        StringBuilder resp = new StringBuilder("[");
        synchronized (lastEvents) {
            for (int i = 0; i < lastEvents.size(); i++) {
                resp.append(lastEvents.get(i));
                if (i < lastEvents.size() - 1) resp.append(",");
            }
        }
        resp.append("]");

        exchange.sendResponseHeaders(200, resp.toString().getBytes(StandardCharsets.UTF_8).length);
        exchange.getResponseBody().write(resp.toString().getBytes(StandardCharsets.UTF_8));
        exchange.close();
    }
}
