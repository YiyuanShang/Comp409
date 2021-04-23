package exercises;

import java.util.LinkedList;

public class WaitNotifySpecificImplMonitor {
    private LinkedList<Thread> waiters = new LinkedList<>();
    private Thread woken;

    public synchronized void waitSpecifically(){
        waiters.add(Thread.currentThread());
        do{
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }while(woken != Thread.currentThread());
        waiters.remove(woken);
        woken = null;
        notifyAll();
    }

    public synchronized void notifySpecific(Thread t){
        while (woken != null || !waiters.contains(t)){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        waiters.remove(t);
        woken = t;
        notifyAll();
    }

}
