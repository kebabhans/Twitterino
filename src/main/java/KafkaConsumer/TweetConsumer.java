package KafkaConsumer;

import TwitterResponse.TwitterResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

public class TweetConsumer {
    Logger logger = LoggerFactory.getLogger(TweetConsumer.class);

    public static void main(String[] args) {
        System.out.println("Starting KafkaConsumer");
        TweetConsumer kafkaConsumer = new TweetConsumer();
        kafkaConsumer.start();
        System.out.println("Exiting KafkaConsumer");
    }

    void start() {
        String bootstrapServers = "127.0.0.1:29092";
        String groupId = "my-sixth-application";
        String topic = "tweets";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer .class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // Create the consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // Subscribe consumer to out topics
        consumer.subscribe(Collections.singleton(topic));

        // Init mapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Ask for data
        HttpClient httpClient = HttpClient.newBuilder().build();
        while(true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for(ConsumerRecord<String, String> record : records) {
                String tweetKey = record.key();
                String tweetPayloadString = record.value();

                System.out.println("Key: " + tweetKey+", Value: " + tweetPayloadString);

                TwitterResponse tweet = null;
                try {
                    // Deserialize
                    tweet = objectMapper.readValue(tweetPayloadString, TwitterResponse.class);
                } catch (JsonProcessingException e) {
                    System.out.println(String.format("Failed to deserialize payload from Kafka under key: %s", tweetKey));
                    continue;
                }

                // Post it to ElasticSearch
                HttpRequest.Builder builder = HttpRequest.newBuilder();
                String fullUrl = "http://localhost:9200/twitterdata/_doc";
                try {
                    String requestBody = objectMapper.writeValueAsString(tweet);
                    HttpRequest httpRequest = builder.uri(URI.create(fullUrl)).setHeader("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

                    HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

//                    HttpResponse<String> httpResponse = httpClient.send(builder.uri(URI.create("http://localhost:9200")).GET().build(), HttpResponse.BodyHandlers.ofString());
                    if (httpResponse.statusCode() != 201) {
                        System.out.println("Failed to create entry in ElasticSearch");
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Succesffully inserted document into ElasticSearch"+ new Date().toString());
            }
        }
    }
}
