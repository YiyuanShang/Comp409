package exercises;

import java.util.concurrent.Semaphore;

public class ReusableBarrier {
    Semaphore lock;
    int countIn;
    int countOut;
    int threadNum;

    ReusableBarrier(int n){
        lock = new Semaphore(1);
        countIn = n;
        countOut = n;
        threadNum = n;
    }

    public void arrive() throws InterruptedException {
        while( countOut != threadNum);

        lock.acquire();
        countIn--;
        if (countIn == 0){ // last one thread
            countIn = threadNum;
            countOut = 1;
            lock.release();
        }else{
            lock.release();

            while (countIn != threadNum);

            lock.acquire();
            countOut++;
            lock.release();
        }
    }
}
