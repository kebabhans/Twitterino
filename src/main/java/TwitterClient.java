import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;


public class TwitterClient {

    public static void main(final String[] args) throws Exception {
        String bearerToken = args[0];

        // Construct request
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder
                .uri(new URI("https://api.twitter.com/2/tweets/search/stream?tweet.fields=created_at"))
                .GET().header("authorization","Bearer " + bearerToken);
        HttpRequest request = builder.build();

        // Consume stream of tweets, and pass them on to FakeTweetPublisher
        CompletableFuture<HttpResponse<Void>> response = HttpClient.newBuilder()
                .build()
                .sendAsync(request, HttpResponse.BodyHandlers.fromLineSubscriber(new KafkaPublisher()));
        System.out.println("Awaiting result");
        response.get();
        System.out.println("Stopping");
    }
}
