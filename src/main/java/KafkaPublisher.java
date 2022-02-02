import TwitterResponse.TwitterResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.Future;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

/**
 * Places payloads (tweets) on Kafka topic
 */
public class KafkaPublisher implements Flow.Subscriber<String> {
    Logger logger = LoggerFactory.getLogger(KafkaPublisher.class);

    private Subscription subscription;

    private String dkPolRuleId = "1467946401745326086";
    private String cnnTweetsRuleId = "1467946401745326085";
    private String coronaTweetsRuleId = "1478057803189305345";
    private String omikronTweetsRuleId = "1478058431814713353";
    private String goalsTweetsRuleId = "1478436180861542406";
    private Map<String, String> topicMappings = Map.of(
            dkPolRuleId, "dkPol",
            cnnTweetsRuleId, "cnn",
            coronaTweetsRuleId, "corona",
            omikronTweetsRuleId, "omikron",
            goalsTweetsRuleId, "goals"
    );

    private KafkaProducer<String, String> kafkaProducer = null;

    ObjectMapper mapper = new ObjectMapper();

    public KafkaPublisher(){
        // Establish connection to Kafka

        // Create producer properties
        Properties properties = new Properties();
        String bootstrapServers = "127.0.0.1:29092";
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Make producer safe
        properties.setProperty(ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");

        // Apply message compression to payload
        properties.setProperty(COMPRESSION_TYPE_CONFIG, "snappy");

        properties.setProperty(LINGER_MS_CONFIG, "20"); // Allow for (maximim of) 20ms delay before sending a message
        properties.setProperty(BATCH_SIZE_CONFIG, Integer.toString(32*1024)); // Increase batch-size (default 16kB)

        // Create producer
        this.kafkaProducer = new KafkaProducer<>(properties);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Enter shutdown hook, asking kafkaproducer to shutdown");
            this.kafkaProducer.close();
            logger.info("kafkaproducer closed, exiting");
        }));
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(String item) {
        Timestamp timestamp = new Timestamp(new Date().getTime());
        System.out.println(String.format("Received something: %s, at %s", item, timestamp));

        // Twitter will send us empty strings periodically to keep connection alive
        if (item.isEmpty()) {
            System.out.println(String.format("Received empty string at %s, returning", timestamp));

            this.subscription.request(1); // Request another item
            return;
        }

        // Convert to JSON
        TwitterResponse response = null;
        String responseAsString = null;
        try {
            response = this.mapper.readValue(item, TwitterResponse.class);
            responseAsString = mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            System.out.println("JSON error when dealing with: " + item);

            // TODO: Figure out if below is the right approach
            return;
        }

        // Look up topic
        String matchedRuleId = response.matchedRules.get(0).id;
        String key = topicMappings.get(matchedRuleId);

        // Produce payload to post to Kafka
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("tweets", key, responseAsString);

        // POST to Kafka
        Future<RecordMetadata> sendResult = this.kafkaProducer.send(producerRecord);
        try {
            RecordMetadata result = sendResult.get();
            System.out.println(result.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Sent record to Kafka: " + responseAsString);

        // If everything went well, request another item
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("We got an error: " + throwable.toString());
    }

    @Override
    public void onComplete() {
        System.out.println("Now done, apparently!");
    }
}
