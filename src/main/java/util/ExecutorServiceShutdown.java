package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class ExecutorServiceShutdown {

    private static final Logger LOG = LoggerFactory.getLogger(ExecutorServiceShutdown.class);

    private ExecutorServiceShutdown() {
    }

    public static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            boolean isTerminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
            if (!isTerminated) {
                executorService.shutdownNow();
                boolean isFinallyTerminated = executorService.awaitTermination(10, TimeUnit.SECONDS);
                if(!isFinallyTerminated) {
                    LOG.error("Executor did not terminate");
                }
            }
        } catch (InterruptedException interruptedException) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
