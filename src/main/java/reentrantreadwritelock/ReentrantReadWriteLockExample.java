package reentrantreadwritelock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ReentrantReadWriteLockExample {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        SharedData sharedData = new SharedData();

        executorService.submit(new Reader(sharedData));
        executorService.submit(new Reader(sharedData));

        executorService.submit(new Writer(sharedData));
        executorService.submit(new Writer(sharedData));

        shutdown(executorService);
    }


    private static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            boolean isTerminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (!isTerminated) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}

class Reader implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Reader.class);

    private final SharedData sharedData;

    Reader(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String key = sharedData.get("KEY");
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000));
                LOG.info("Received {}", key);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class Writer implements Runnable {

    private final SharedData sharedData;

    Writer(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    @Override
    public void run() {
        while (true) {
            try {
                sharedData.put("KEY", String.format("VALUE %s", ThreadLocalRandom.current().nextInt(1000)));
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000));
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

class SharedData {

    private static final Logger LOG = LoggerFactory.getLogger(SharedData.class);

    private final ReentrantReadWriteLock reentrantReadWriteLock;
    private final Map<String, String> internalData;

    SharedData() {
        this.reentrantReadWriteLock = new ReentrantReadWriteLock();
        this.internalData = new HashMap<>();
    }

    String get(String key) {
        try {
            reentrantReadWriteLock.readLock().lock();
            LOG.info("Read lock acquired");
            return internalData.get(key);
        } finally {
            reentrantReadWriteLock.readLock().unlock();
            LOG.info("Read lock released");
        }
    }

    String put(String key, String value) {
        try {
            LOG.info("Write lock acquired");
            reentrantReadWriteLock.writeLock().lock();
            return internalData.put(key, value);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
            LOG.info("Write lock released");
        }
    }
}
