package Problem1;

import java.util.concurrent.Semaphore;

public class CyclicBarrier {
    public final static Object synch = new Object();

    Semaphore sema;

    public CyclicBarrier(int parties) {
        sema = new Semaphore(parties);
    }

    int await() throws InterruptedException {
        synchronized(synch) {
            sema.acquire();
            int pos = sema.availablePermits();

            if (sema.availablePermits() > 0) {
                System.out.println("Waiting");
                synch.wait();
            }

            sema.release();
            synch.notifyAll();

            return pos;
        }
    }
}