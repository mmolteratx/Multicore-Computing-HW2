package q2;

import org.junit.Test;
import java.util.HashMap;

public class ExampleMonkey implements Runnable{

    // viewable by all threads
    public static volatile HashMap<Long, Monkey> monkeys = new HashMap<Long, Monkey>();
    public static volatile Integer numTripsAcross;

    @Test
    public void main() throws InterruptedException {
        main(10, 6);
    }

    public void main(int numMonkeys, int tripsAcross) throws InterruptedException {

        numTripsAcross = tripsAcross;

        // init and start all the threads/monkeys
        ExampleMonkey runnable = new ExampleMonkey();
        Thread[] threads = new Thread[numMonkeys];
        for (int i = 0; i < numMonkeys; i++) {
            threads[i] = new Thread(runnable, Integer.toString(i));
        }
        for (Thread thread : threads) {
            thread.start();
        }

        // wait for threads to finish
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        int count = 0;

        long ID = Thread.currentThread().getId();
        Monkey monkey = new Monkey(ID);
        monkeys.put(ID, monkey);

        // start on one side, also set a Kong
        int direction = 0;
        if (Thread.currentThread().getName().equals("0")) {
            direction = -1;
            System.out.println("Monkey"+ID+" is KONG!!!!");
        }
        while(count < numTripsAcross) {
            try {
                int step = 0;
                int maxSteps = 10;
                monkey.ClimbRope(direction);
                while (step < 10) {
                    System.out.println("    There are "+monkey.getNumMonkeysOnRope()+" monkeys on the bridge! ID:"+ID);
                    //System.out.println("Monkey" + ID + " is " + step + "/" + maxSteps + " of the way across");
                    step++;
                }
                monkey.LeaveRope();
                // if not Kong, switch direction
                if (direction != -1) {
                    if (direction == 0)
                        direction = 1;
                    else
                        direction = 0;
                }
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
