package Problem1;

//import java.util.concurrent.Problem1.CyclicBarrier;

public class CyclicBarrierExample {
    // create a cyclic barrier that will wait for NO_OF_THREADS
    private static final int NO_OF_THREADS = 8;
    private CyclicBarrier barrier = new CyclicBarrier(NO_OF_THREADS);

    public static void main(String[] args) throws InterruptedException {
        CyclicBarrierExample instance = new CyclicBarrierExample();
        instance.init();
    }

    public void init() throws InterruptedException {
        // create NO_OF_THREADS that will call await on the cyclic barrier
        for (int i=0; i<NO_OF_THREADS; ++i) {
            Thread th = new Thread(new MyWorkerThread(), "Worker" + i);
            th.start();

            // to help visualise I add a delay between creation
            Thread.sleep(1000);
        }
    }

    private class MyWorkerThread implements Runnable {
        @Override
        public void run() {
            System.out.println("Thread started");

            try {
                int i = barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Barrier unlocked");
        }
    }
}
