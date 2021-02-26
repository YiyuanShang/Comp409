package exercises;

/**
 * a Lock class with acquire and release that implements a priority inheritance solution
 * allow for recursive lock acquisition
 */
public class PriorityInheritanceImplLock {
    private int originalPriority;
    private int locked;
    private Thread owner;

    public synchronized void acquire(){
        while (owner != null && owner != Thread.currentThread()){
            if (Thread.currentThread().getPriority() > owner.getPriority()){
                owner.setPriority(Thread.currentThread().getPriority());
            }
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
        notifyAll(); // wake others to recheck the priorities
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
