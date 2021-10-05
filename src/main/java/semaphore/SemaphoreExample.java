package semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.*;

class SemaphoreExample {

    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1);
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        SharedData sharedData = new SharedData(0);

        executorService.submit(new Randomizer(sharedData, semaphore));
        executorService.submit(new Randomizer(sharedData, semaphore));
        executorService.submit(new Randomizer(sharedData, semaphore));
        executorService.submit(new Randomizer(sharedData, semaphore));

        shutdown(executorService);
    }

    private static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            boolean isTerminated = executorService.awaitTermination(5, TimeUnit.SECONDS);
            if (!isTerminated) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}


class Randomizer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Randomizer.class);
    private final SharedData sharedData;
    private final Semaphore semaphore;

    Randomizer(SharedData sharedData, Semaphore semaphore) {
        this.sharedData = sharedData;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                semaphore.acquire();
                LOG.info("Semaphore acquired");
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
            int i = ThreadLocalRandom.current().nextInt(100);
            sharedData.setData(i);
            LOG.info("Data set {}", i);
            semaphore.release();
            LOG.info("Semaphore released");
        }
    }
}

class SharedData {
    private volatile int data;

    SharedData(int data) {
        this.data = data;
    }

    int getData() {
        return data;
    }

    void setData(int data) {
        this.data = data;
    }
}
