package Problem1;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class CyclicBarrier {

    Semaphore sema;
    AtomicBoolean rel = new AtomicBoolean(false);

    public CyclicBarrier(int parties) {
        sema = new Semaphore(parties);
    }

    int await() throws InterruptedException {
        sema.acquire();
        int pos = sema.availablePermits();

        while (sema.availablePermits() > 0 && !rel.get()) {
        }
        
        if(!rel.get()) {rel.set(true)};
        
        sema.release();
        return pos;
    }
}
