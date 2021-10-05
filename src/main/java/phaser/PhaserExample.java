package phaser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

class PhaserExample {

    private static final Logger LOG = LoggerFactory.getLogger(PhaserExample.class);

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Phaser phaser = new Phaser(1);

        executorService.submit(new Task(1, phaser));
        executorService.submit(new Task(2, phaser));
        executorService.submit(new Task(3, phaser));

        phaser.arriveAndAwaitAdvance();
        LOG.info("Phase {} done ", phaser.getPhase());
        phaser.arriveAndDeregister();

        executorService.submit(new Task(1, phaser));
        executorService.submit(new Task(2, phaser));
        executorService.submit(new Task(3, phaser));

        phaser.arriveAndAwaitAdvance();
        LOG.info("Phase {} done ", phaser.getPhase());
        phaser.arriveAndDeregister();

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


class Task implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Task.class);

    private static int counter = 0;

    private final Phaser phaser;
    private final int number;

    Task(int number, Phaser phaser) {
        this.phaser = phaser;
        this.number = counter++;
    }

    @Override
    public void run() {
        phaser.register();
        LOG.info("waiting for others");
        phaser.arriveAndAwaitAdvance();
        LOG.info("finished waiting");
        phaser.arriveAndDeregister();
    }
}
