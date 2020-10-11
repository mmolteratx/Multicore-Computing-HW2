package q2;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class Monkey {
    /* locks to signal if it is okay to cross the bridge */
    static final ReentrantLock monitorLock = new ReentrantLock();
    static final Condition go0  = monitorLock.newCondition();
    static final Condition go1  = monitorLock.newCondition();
    static final Condition goKong  = monitorLock.newCondition();

    /* if Kong wants to cross the bridge */
    static final AtomicBoolean kongWants = new AtomicBoolean(false);
    /* if there is a need to go left across the bridge */
    static final AtomicBoolean need0 = new AtomicBoolean(false);
    /* if there is a need to go right across the bridge */
    static final AtomicBoolean need1 = new AtomicBoolean(false);
    /* current direction for crossing the bridge */
    static final AtomicInteger crossingDirection = new AtomicInteger(0);
    /* number of monkeys on bridge */
    static final AtomicInteger numMonkeys = new AtomicInteger(0);

    /* if monkey is on bridge */
    final AtomicBoolean onBridge = new AtomicBoolean(false);
    /* monkeyID */
    final Long monkeyID;

    //constructor
    public Monkey(long ID) {
      monkeyID = ID;
    }

    /**
     * A monkey calls the method when it arrives at the river bank and wants to climb
     * the rope in the specified direction (0 or 1); Kong’s direction is -1.
     * The method blocks a monkey until it is allowed to climb the rope.
     */
    public void ClimbRope(int direction) throws InterruptedException {
        monitorLock.lock();
        //System.out.println("    There are "+getNumMonkeysOnRope()+" monkeys on the bridge!");
        if (direction == 0) {
            need0.set(true);
            //System.out.println("Monkey" + monkeyID + " wants to climb rope 0!");
        }
        if (direction == 1) {
            need1.set(true);
            //System.out.println("Monkey" + monkeyID + " wants to climb rope 1!");
        }
        if (direction == -1) {
            kongWants.set(true);
            //System.out.println("Monkey"+monkeyID +" wants to climb rope (KONG)!");
        }
        try {
            // while I have not been able to cross the bridge
            TestBridge(direction);
            while(!onBridge.get()) {
                // if I am Kong
                if (direction == -1) {
                    //System.out.println("Monkey"+monkeyID+" aka Kong is waiting!");
                    goKong.await();
                }
                else if (direction == 0){
                    //System.out.println("Monkey" + monkeyID + " is waiting to go 0!");
                    go0.await();
                }
                else {
                    //System.out.println("Monkey"+monkeyID+" is waiting to go 1!");
                    go1.await();
                }
                //System.out.println("Monkey" + monkeyID + " is testing conditions");
                TestBridge(direction);
            }
        } finally{
            //System.out.println("    There are "+getNumMonkeysOnRope()+" monkeys on the bridge!");
            monitorLock.unlock();
        }
    }

    /** see if a monkey can cross the bridge or not
     * also works for Kong (assume only one Kong at a time)
     */
    public synchronized void TestBridge(int direction) {
        //test if Kong wants go
        if (kongWants.get()) {
            // and I am Kong
            if (numMonkeys.get() == 0 && direction == -1) {
                numMonkeys.getAndIncrement();
                //System.out.println("Monkey" + monkeyID + " aka Kong can go!");
                // set direction for bridge
                crossingDirection.set(direction);
                //System.out.println("  Bridge direction set to KONG");
                onBridge.set(true);
            }
        // Kong is not waiting and there is space
        } else {
            // and no one is on bridge
            if (numMonkeys.get() == 0) {
                numMonkeys.getAndIncrement();
                //System.out.println("Monkey" + monkeyID + " can go anywhere!");
                // set direction for bridge
                crossingDirection.set(direction);
                //System.out.println("  Bridge direction set to " + direction);
                onBridge.set(true);
            }
            // others on bridge are going my way
            else if (numMonkeys.get() > 0 && numMonkeys.get() < 3 && direction == crossingDirection.get()) {
                numMonkeys.getAndIncrement();
                //System.out.println("Monkey" + monkeyID + " can go in the same direction!");
                onBridge.set(true);
            }
        }
    }

    /** After crossing the river, every monkey calls this method which
     * allows other monkeys to climb the rope.
     */
    public void LeaveRope() {
        monitorLock.lock();
        //System.out.println("    There are "+getNumMonkeysOnRope()+" monkeys on the bridge!");
        // update variables
        onBridge.set(false);
        numMonkeys.getAndDecrement();

        NextBridge();

        //System.out.println("    There are "+getNumMonkeysOnRope()+" monkeys on the bridge!");
        monitorLock.unlock();
    }

    /** find next direction of monkeys to cross
     * also works for Kong (assume only one Kong at a time)
     */
    public synchronized void NextBridge() {
        // I am Kong
        if(crossingDirection.get() == -1) {
            //System.out.println("Monkey"+monkeyID+" aka Kong is done!");
            kongWants.set(false);
            // and the 0 crossing monkeys are waiting first
            if (need0.get()) {
                go0.signal();
                go1.signal();
            }
            // try for any 1 crossing monkeys waiting first
            else {
                go1.signal();
                go0.signal();
            }
        }
        // if I am not Kong
        else {
            //System.out.println("Monkey"+monkeyID+" is done!");
            // and and I am the last one over
            if (numMonkeys.get() == 0) {
                // and Kong is waiting
                if (kongWants.get()) {
                    goKong.signal();
                }
                // and the 0 crossing monkeys are waiting first
                else if (need0.get() && crossingDirection.get() == 1) {
                    need1.set(false);
                    go0.signal();
                }
                // and the 1 crossing monkeys are waiting first
                else if (need1.get() && crossingDirection.get() == 0) {
                    need0.set(false);
                    go1.signal();
                }
                // assume this took too long, wake any thread that might want to go
                else {
                    go0.signalAll();
                    go1.signalAll();
                }

            }
            // if there is still space to cross
            else if (numMonkeys.get() < 3) {
                // try to wake others going the same direction
                if (crossingDirection.get() == 1) {
                    go1.signalAll();
                } else {
                    go0.signalAll();
                }
            }
        }
    }

    /**
     * Returns the number of monkeys on the rope currently for test purpose.
     *
     * @return the number of monkeys on the rope
     *
     * Positive Test Cases:
     * case 1: normal monkey (0 and 1)on the rope, this value should <= 3, >= 0
     * case 2: when Kong is on the rope, this value should be 1
     */
    public int getNumMonkeysOnRope() {
        int num = numMonkeys.intValue();
        return num;
    }
}
