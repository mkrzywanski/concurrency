package completionservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.*;

import static util.ExecutorServiceShutdown.shutdown;

class ExecutorCompletionServiceExample {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorCompletionServiceExample.class);
    private static final Random RANDOM = new SecureRandom();
    private static final int TASKS_AMOUNT = 10;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        var completionService = new ExecutorCompletionService<>(executorService);

        for (int i = 0; i < TASKS_AMOUNT; i++) {
            completionService.submit(() -> {
                int sleepTime = RANDOM.nextInt(2000);
                Thread.sleep(sleepTime);
                return sleepTime;
            });
        }
        for (int i = 0; i < TASKS_AMOUNT; i++) {
            var future = completionService.take();
            LOG.info("Task finished : {}. Time {} milliseconds", future.isDone(), future.get());
        }
        shutdown(executorService);
    }

}
