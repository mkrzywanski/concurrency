package transferqueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

import static util.ExecutorServiceShutdown.shutdown;

/**
 * Direct hands-off. TransferQueue is based on CAS. SynchronousQueue is based on double queues.
 */
class TransferQueueExample {

    public static void main(String[] args) throws InterruptedException {
        TransferQueue<Data> transferQueue = new LinkedTransferQueue<>();
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        executorService.submit(new Consumer(transferQueue));

        executorService.submit(new Producer(transferQueue));
        executorService.submit(new Producer(transferQueue));
        executorService.submit(new Producer(transferQueue));

        Thread.sleep(4000);

        shutdown(executorService);
    }

}

class Producer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    private final TransferQueue<Data> transferQueue;

    Producer(TransferQueue<Data> transferQueue) {
        this.transferQueue = transferQueue;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            try {
                LOGGER.info("transfer data i");
                long currentTimeMillis = System.currentTimeMillis();
                transferQueue.transfer(new Data(Thread.currentThread().getName(), i));
                LOGGER.info("Blocked for " + (System.currentTimeMillis() - currentTimeMillis));
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class Consumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

    private final TransferQueue<Data> transferQueue;

    Consumer(TransferQueue<Data> transferQueue) {
        this.transferQueue = transferQueue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Data take = transferQueue.take();
                LOG.info("Received " + take);
                Thread.sleep(500);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class Data {
    private final String producerName;
    private final int data;

    Data(String producerName, int data) {
        this.producerName = producerName;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Data{" +
                "producerName='" + producerName + '\'' +
                ", data=" + data +
                '}';
    }
}