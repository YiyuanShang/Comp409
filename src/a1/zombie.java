package a1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class zombie {
    static List<Friend> friends = new ArrayList<>();
    static int friendNum; // number of friend threads, args[0]
    static int threshold; // threshold, args[1]
    static int enteredTotalNum; // total number of entered zombies
    static int removedNum = 0; // number of removed zombies
    static int currentNum; // number of current zombies
    static long period = 20 * 1000; // default running time is 20s
    static boolean running = true; // flag variable to control friend threads running

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
                // ask friend threads to stop letting zombie in while counting and making a decision
                setFriendsLetInStatus(false);
                countZombie();

                // Main thread decision making starts
                // current zombie number is beyond threshold
                if (currentNum >= threshold){
                    System.out.println("Current zombie number is beyond threshold");
                    System.out.println("Main thread asks Friend threads to stop letting zombie in");
                }else{
                    if (currentNum <= threshold/2){
                        setFriendsLetInStatus(true);
                    }
                }
                //Main thread decision making ends

                if (currentNum > 0){
                    eliminateZombie();
                }

                // check the total number of current zombies every 1s
                Thread.sleep(1000);

            }

            // shut down all friend threads
            running = false;
            // main thread waits until all friend threads shut down
            for (Friend friend : friends){
                friend.myThread.join();
            }
            System.out.println("\nFinal Result ->"
                    + "\t FriendNum:" + friendNum
                    + "\t Threshold:" + threshold
                    + "\t RemovedNum:" + removedNum
                    + "\t Period:" + period/1000 + "s"
                    + "\t Throughput:" + (float)removedNum/(float)(period/1000) + " zombie/second");
            System.exit(0);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

    }

    synchronized static void countZombie(){
        enteredTotalNum = 0;
        for (Friend friend: friends){
            enteredTotalNum += friend.entered;
        }
        currentNum = enteredTotalNum - removedNum;
        System.out.println("Main thread counting -> "
                            + "\t enteredTotal:" + enteredTotalNum
                            + "\t removed:" + removedNum
                            + "\t current:" + currentNum);
    }

    static void eliminateZombie(){
        // obtain a random number between [1,10]
        Random rand = new Random();
        int probability = rand.nextInt(10) + 1;
        // remove a zombie with 40% probability
        if (probability <= 4){
            removedNum ++;
            System.out.println("Main thread removed:" + removedNum);
        }
    }

    static void setFriendsLetInStatus(Boolean pCanLetIn){
        for(Friend friend:friends){
            friend.canLetIn = pCanLetIn;
        }
    }


    static class Friend implements Runnable{
        Thread myThread;
        boolean canLetIn = true;
        int entered = 0;

        Friend(){
            myThread = new Thread(this);
            myThread.start();
        }
        @Override
        public void run() {
            try {
                while(running) {
                    // let zombie in every 10 ms
                    if (canLetIn) {
                        letInZombie();
                    }
                    Thread.sleep(10);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        void letInZombie(){
            // obtain a random number between [1-10]
            Random rand = new Random();
            int probability = rand.nextInt(10) + 1;
            // let a zombie in with 10% probability
            if (probability == 1){
                entered ++;
                System.out.println("Friend thread " + myThread.getId() + " already entered:" + entered);
            }
        }
    }
}
