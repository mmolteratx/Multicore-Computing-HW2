package Problem1;

import java.util.concurrent.Semaphore;

public class CyclicBarrier {

    Semaphore sema;

    public CyclicBarrier(int parties) {
        sema = new Semaphore(parties);
    }

    int await() throws InterruptedException {
        sema.acquire();
        int pos = sema.availablePermits();

        while (sema.availablePermits() > 0) {
        }

        sema.release();
        return pos;
    }
}
