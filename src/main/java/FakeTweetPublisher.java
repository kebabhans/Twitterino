import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.Flow;

public class FakeTweetPublisher implements Flow.Subscriber<String> {
    private Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(String item) {
        Timestamp timestamp = new Timestamp(new Date().getTime());

        if (item.isEmpty()) {
            System.out.println(String.format("Received empty string at %s, returning", timestamp));
        } else {
            System.out.println(String.format("Received something: %s, at %s", item, timestamp));
        }

        this.subscription.request(1); // Request another item
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
