package exercises;

/**
 * a Lock class with acquire and release that implements a priority ceiling solution
 */
public class PriorityCeilingImplLock {
    private int originalPriority;
    private int locked;
    private Thread owner;

    public synchronized void acquire(){
        while (owner != null && owner != Thread.currentThread()){
            owner.setPriority(Thread.MAX_PRIORITY);
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        owner = Thread.currentThread();
        if (locked == 0){
            originalPriority = owner.getPriority();
        }
        locked++;
    }

    public synchronized void release(){
        locked--;
        if (locked == 0){
            Thread.currentThread().setPriority(originalPriority);
            owner = null;
            notify(); // wake someone else
        }
    }
}
