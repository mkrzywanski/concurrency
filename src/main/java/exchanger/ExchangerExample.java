package exchanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class ExchangerExample {

    public static void main(String[] args) throws InterruptedException {
        Exchanger<Data> exchanger = new Exchanger<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2, new ActorThreadFactory());

        Future<?> actor1 = executorService.submit(new Actor(exchanger));
        Future<?> actor2 = executorService.submit(new Actor(exchanger));

        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        actor1.cancel(true);
        actor2.cancel(true);

        shutdown(executorService);
    }


    private static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            boolean isTerminated = executorService.awaitTermination(20, TimeUnit.SECONDS);
            if (!isTerminated) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}


class Actor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Actor.class);
    private final Exchanger<Data> exchanger;

    Actor(Exchanger<Data> exchanger) {
        this.exchanger = exchanger;
    }

    @Override
    public void run() {
        String actorName = Thread.currentThread().getName();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Data data = exchanger.exchange(new Data(actorName));
                LOG.info("{} received data from {}", actorName, data.getSourceActorName());
                Thread.sleep(TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class ActorThreadFactory implements ThreadFactory {

    private final AtomicInteger atomicInteger = new AtomicInteger(1);

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "Actor " + atomicInteger.getAndIncrement());
    }
}

class Data {
    private final String sourceActorName;

    Data(String sourceActorName) {
        this.sourceActorName = sourceActorName;
    }

    String getSourceActorName() {
        return sourceActorName;
    }

    @Override
    public String toString() {
        return "Data{" +
                "sourceActorName='" + sourceActorName + '\'' +
                '}';
    }
}
