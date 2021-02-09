package a1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class zombie {
    static List<Friend> friends = new ArrayList<>();
    static int friendNum;
    static int threshold;

    static int enteredTotalNum;
    static int removedNum = 0;
    static int currentNum;
    static long period = 20 * 1000; // 20s
    static boolean running = true;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        // get friend number and threshold from cmd
        if (args.length>0){
            friendNum = Integer.parseInt(args[0]);
            threshold = Integer.parseInt(args[1]);
        }

        // start friends' threads
        for (int i =0; i<friendNum; i++){
            Friend friend = new Friend();
            friends.add(friend);
        }
        try{
            while (System.currentTimeMillis() - startTime <= period){

                countZombie();
                System.out.println("[Main thread counting ends]");
                System.out.println("[Main thread making decision starts]");
                // current zombie number beyond threshold
                if (currentNum >= threshold){
                    System.out.println("Current zombie number beyond threshold");
                }else{
                    if (currentNum <= threshold/2){
                        setFriendLetInStatus(true);
                    }
                }
                System.out.println("[Main thread making decision ends]");
                if (currentNum > 0){
                    eliminateZombie();
                }

                Thread.sleep(1000);

            }

            // shut down all friend threads
            running = false;
            // main thread waits until all friend threads shut down
            for (Friend friend : friends){
                friend.myThread.join();
            }
            System.out.println("\nRemovedNum:" + removedNum
                    + "\tPeriod:" + period/1000 + "s" +
                    "\tThroughput:" + (float)removedNum/(float)(period/1000));
            System.exit(1);
        }catch (InterruptedException e){
            e.printStackTrace();
        }



    }

    synchronized static void countZombie(){
        System.out.println("[Main Thread counting starts]");
        enteredTotalNum = 0;
        for (Friend friend: friends){
            // ask friend thread to stop letting zombie in
            friend.canLetIn = false;
            enteredTotalNum += friend.entered;
        }
        currentNum = enteredTotalNum - removedNum;
        System.out.println("Main thread enteredTotal:" + enteredTotalNum
                            + "\tremoved:" + removedNum
                            + "\tcurrent:" + currentNum
                            + "\tthreshold:" + threshold);
    }

    static void eliminateZombie(){
        Random rand = new Random();
        int probability = rand.nextInt(10) + 1;
        if (probability <= 4){
            removedNum ++;
        }
        System.out.println("Main thread removed:" + removedNum);

    }

    static void setFriendLetInStatus(Boolean pCanLetIn){
        for(Friend friend:friends){
            friend.setCanLetIn(pCanLetIn);
        }
    }


    private static class Friend implements Runnable{
        Thread myThread;
        boolean canLetIn = true;
        int entered = 0;

        Friend(){
            myThread = new Thread(this);
            System.out.println("Friend thread " + myThread.getId() + " created");
            myThread.start();
        }
        @Override
        public void run() {
            try {
                while(running) {
                    if (canLetIn) {
                        letInZombie();
                        System.out.println("Friend thread " + myThread.getId() + " newly entered:" + entered);
                    } else {
                        System.out.println("Friend thread " + myThread.getId() + " stops letting zombie in, already entered:" + entered);
                    }

                    Thread.sleep(10);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        void letInZombie(){
            // obtain a number between [1-10]
            Random rand = new Random();
            int probability = rand.nextInt(10) + 1;
            if (probability == 1){
                entered ++;
            }
        }

        synchronized void setCanLetIn(Boolean pCanLetIn){
            canLetIn = pCanLetIn;
        }
    }
}
