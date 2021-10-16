package cyclicbarrier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.stream.IntStream;

import static util.ExecutorServiceShutdown.shutdown;

class CyclicBarrierExample {

    private static final int THREADS = 10;
    private static final Logger LOG = LoggerFactory.getLogger(CyclicBarrierExample.class);

    public static void main(String[] args) {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(THREADS, () -> LOG.info("All threads released"));

        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);

        IntStream.range(0, THREADS)
                .forEach(value -> executorService.submit(new Task(cyclicBarrier, value)));

        shutdown(executorService);
    }

    private static class Task implements Runnable {
        private final CyclicBarrier cyclicBarrier;
        private final int value;

        public Task(CyclicBarrier cyclicBarrier, int value) {
            this.cyclicBarrier = cyclicBarrier;
            this.value = value;
        }

        @Override
        public void run() {
            LOG.info("Execute task " + value);
            try {
                LOG.info("waiting");
                cyclicBarrier.await();
                LOG.info("released");
            } catch (InterruptedException | BrokenBarrierException interruptedException) {
                Thread.currentThread().interrupt();
            }

        }
    }
}
