package locksupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

class LockSupportExample {

    private static final Logger LOG = LoggerFactory.getLogger(LockSupportExample.class);

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Thread waitingThread = new Thread(() -> {
            LOG.info("Working");
            countDownLatch.countDown();
            LockSupport.parkNanos(10000);
            LOG.info("Unparked");
        });

        Thread unlockingThread = new Thread(() -> {
            try {
                LOG.info("Awaiting other thread is ready");
                countDownLatch.await();
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            } finally {
                LockSupport.unpark(waitingThread);
            }
        });

        waitingThread.start();
        unlockingThread.start();
    }
}
