package delayedqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.*;

import static util.ExecutorServiceShutdown.shutdown;

/**
 * Producer will put 10 elements on rhe queue
 * Those elements will be consumed every 1 second by Consumer
 */
class DelayQueueExample {

    private static final Logger LOG = LoggerFactory.getLogger(DelayQueueExample.class);
    private static final int THREADS = 2;

    public static void main(String[] args) {
        BlockingQueue<Element>  queue = new DelayQueue<>();
        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        executorService.submit(new Producer(queue));
        executorService.submit(new Consumer(queue));

        shutdown(executorService);

    }
}

class Producer implements Runnable {


    private static final Logger LOG = LoggerFactory.getLogger(Producer.class);
    private final BlockingQueue<Element> queue;

    Producer(BlockingQueue<Element> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                Element e = new Element(i, i);
                LOG.info("Put element {}", e);
                queue.put(e);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class Consumer implements Runnable{

    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);
    private final BlockingQueue<Element> queue;

    Consumer(BlockingQueue<Element> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                Element take = queue.take();
                LOG.info("consuming {}", take);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class Element implements Delayed {

    private final int data;
    private final Instant creationTime;
    private final int secondsDelay;

    Element(int data, int secondsDelay) {
        this.data = data;
        this.secondsDelay = secondsDelay;
        this.creationTime = Instant.now();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        Instant timePast = Instant.now().minusMillis(this.creationTime.toEpochMilli());
        long millisDelay = TimeUnit.SECONDS.toMillis(secondsDelay);
        long delay = millisDelay - timePast.toEpochMilli();
        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

    @Override
    public String toString() {
        return "Element{" +
                "data=" + data +
                ", creationTime=" + creationTime +
                ", secondsDelay=" + secondsDelay +
                '}';
    }
}
