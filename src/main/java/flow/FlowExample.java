package flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.IntStream;

public class FlowExample {
    public static void main(String[] args) throws InterruptedException {
        SubmissionPublisher<Data> submissionPublisher = new SubmissionPublisher<>();
        Subscriber subscriber = new Subscriber();

        submissionPublisher.subscribe(subscriber);
        IntStream.range(0, 10)
                .mapToObj(Data::new)
                .forEach(submissionPublisher::submit);

        Thread.sleep(100);

        submissionPublisher.close();
    }

    private static class Subscriber implements Flow.Subscriber<Data>{

        private static final Logger LOG = LoggerFactory.getLogger(Subscriber.class);

        private Flow.Subscription subscription;

        @Override
        public void onSubscribe(Flow.Subscription subscription) {
            subscription.request(1);
            this.subscription = subscription;
        }

        @Override
        public void onNext(Data item) {
            LOG.info("Received " + item);
            subscription.request(1);
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error(throwable.getMessage());
        }

        @Override
        public void onComplete() {
            LOG.info("DONE");
        }
    }

    private static class Data {
        private final int data;

        private Data(int data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "data=" + data +
                    '}';
        }
    }
}
