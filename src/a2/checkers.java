package a2;

import java.util.*;

public class checkers {
    public static int t = 16; // checker number
    public static int k = 5; //
    public static int n = 50; // action number
    public static int grid = 8;

    public static volatile List<Square> occupied = new ArrayList<>();
    public static volatile List<Square> capturedAt = new ArrayList<>();

    public static void main(String[] args) {
        populatePositions();

        List<CheckerThread> checkers = new ArrayList<>();
        for (int i = 0; i<t; i++){
            Square square = occupied.get(i);
            CheckerThread checker = new CheckerThread(square.x, square.y, "Thread" + (i+1));
            checkers.add(checker);
        }

        for (CheckerThread checkerThread : checkers){
            checkerThread.myThread.start();
        }
        for (CheckerThread checkerThread: checkers){
            try {
                checkerThread.myThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Main Thread is done");


    }

    private static void populatePositions(){
        while (occupied.size() < t){
            int x = (int)(Math.random() * grid + 1);
            int y = (int)(Math.random() * grid + 1);
            for (Square square1 : occupied){
                if (x == square1.x && y == square1.y) break;
            }
            occupied.add(new Square(x, y));
        }
    }


    static class CheckerThread implements Runnable{
        Thread myThread;
        Square currSquare;
        Square waitSquare;
        private int currAction;
        boolean beCaptured;

        public CheckerThread(Integer x, Integer y, String threadId){
            currSquare = new Square(x, y);
            currAction = 0;
            beCaptured = false;
            myThread = new Thread(this, threadId);
            System.out.println(myThread.getName() + ": initializes at " + currSquare.toString());

        }

        @Override
        public void run() {
            while (currAction < n){
                try {
                    // check whether an action (simple move or capture) is possible in 4 directions
                    totalCheck();
                    Thread.sleep(k);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // remove spawn
            occupied.remove(currSquare);
            System.out.println(myThread.getName() + " is DONE!");
        }

        /**
         * check whether an action (simple move or capture) is possible in 4 directions
         */
        void totalCheck(){
            // check whether this checker thread is captured by others
            checkCapture();

            for (int i=0; i<4; i++){
                // choose a direction randomly
                Direction dir = Direction.getRandomDirection();

                if (dir == Direction.NorthEast){
                    boolean didAction = simpleCheck(1, 1, 2, 2);
                    if (didAction)  break;
                }
                if (dir == Direction.SouthEast){
                    boolean didAction = simpleCheck(1, -1, 2, -2);
                    if (didAction)  break;
                }
                if (dir == Direction.SouthWest){
                    boolean didAction = simpleCheck(-1, -1, -2, -2);
                    if (didAction)  break;
                }
                if (dir == Direction.NorthWest){
                    boolean didAction = simpleCheck(-1, 1, -2, 2);
                    if (didAction)  break;
                }
             }
        }

        /**
         * check whether this checker is captured
         */
        void checkCapture(){
            beCaptured = isCapturedAt(currSquare.x, currSquare.y);

            if (beCaptured){
                System.out.println(myThread.getName() + ": captured");
                try {
                    // captured thread sleeps
                    Thread.sleep((long) ((Math.random() * ((4*k - 2*k) + 1)) + 2*k));
                    // captured checker respawns
                    Square respawn = new Square((int)(Math.random() * grid + 1), (int)(Math.random() * grid+ 1));
                    while(isOccupied(respawn.x, respawn.y)){
                        respawn = new Square((int)(Math.random() * grid + 1), (int)(Math.random() * grid+ 1));
                    }
                    occupied.add(respawn);
                    capturedAt.remove(currSquare);
                    currSquare = respawn;
                    System.out.println(myThread.getName() + ": respawning at " + currSquare.toString());

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * check whether the checker can make an action (simple move or capture) in specified direction
         * @param x_adj adjuster to x coordinate if a simple move
         * @param y_adj adjuster to y coordinate if a simple move
         * @param next_x_adj adjuster to x coordinate if a capture
         * @param next_y_adj adjuster to x coordinate if a capture
         * @return whether an action can be done
         */
        boolean simpleCheck(Integer x_adj, Integer y_adj, Integer next_x_adj, Integer next_y_adj) {
            boolean canMove;
            boolean canCapture;

            Integer next_x = currSquare.x + x_adj;
            Integer next_y = currSquare.y + y_adj;

            // check whether next square is inside checkerboard
            if (!isValidSquare(next_x, next_y)) {
                return false;
            }
            // check if a simple move is possible
            Square nextSquare = new Square(next_x, next_y);
            nextSquare.beginCheck(this);
            canMove = !isOccupied(nextSquare.x, nextSquare.y);

            if (canMove) {
                simpleMove(nextSquare);
                nextSquare.endCheck();
                currAction++;
                return true;
            }
            // check if a capture is possible
            else {
                Integer jump_x = currSquare.x + next_x_adj;
                Integer jump_y = currSquare.y + next_y_adj;

                // check whether jump square is inside checkerboard
                if (!isValidSquare(jump_x, jump_y)) {
                    return false;
                }
                Square jumpSquare = new Square(jump_x, jump_y);
                jumpSquare.beginCheck(this);
                canCapture = !isOccupied(jumpSquare.x, jumpSquare.y);
                if (canCapture) {
                    System.out.println(myThread.getName() + ": captures " + nextSquare.toString());
                    captureMove(nextSquare, jumpSquare);
                    nextSquare.endCheck();
                    jumpSquare.endCheck();
                    currAction++;
                    return true;
                }
                return false;
            }

        }

        /**
         * checker makes a simple move to next square
         * @param nextSquare
         */
        void simpleMove(Square nextSquare){
            occupied.remove(currSquare);
            currSquare = nextSquare;
            occupied.add(nextSquare);
            System.out.println(myThread.getName() + ": moves to " +  currSquare.toString());
        }

        /**
         * checker captures another checker in nextSquare
         * then checker jumps to jumpSquare
         * @param nextSquare
         * @param jumpSquare
         */
        void captureMove(Square nextSquare, Square jumpSquare){
            capturedAt.add(nextSquare);
            occupied.remove(nextSquare);
            simpleMove(jumpSquare);
        }

        /**
         * check whether the square is inside the checkerboard
         * @param x x coordinate of the square
         * @param y y coordinate of the square
         * @return whether the square is inside the checkerboard
         */
        private boolean isValidSquare(Integer x, Integer y){
            if (x >=0 && x<=8 && y>=0 && y<=8){
                return true;
            }
            return false;
        }

        /**
         * check whether the square is occupied by a checker
         * @param x
         * @param y
         * @return
         */
        private synchronized boolean isOccupied(Integer x, Integer y){
            try {
                for (Square square : occupied) {
                    if (square.x == x && square.y == y) return true;
                }
                return false;

            }catch(Exception e){
                return true;
            }
        }

        /**
         * check whether the checker in the square is captured
         * @param x
         * @param y
         * @return
         */
        private synchronized boolean isCapturedAt(Integer x, Integer y){
            try {
                for (Square square : capturedAt) {
                    if (square.x == x && square.y == y) return true;
                }
                return false;
            }catch (Exception e){
                return true;
            }
        }



    }

    static class Square{
        private Integer x;
        private Integer y;
        private boolean checking;
        private CheckerThread owner;

        public Square(Integer x, Integer y){
            this.x = x;
            this.y = y;
            this.checking = false;
        }

        public synchronized void beginCheck(CheckerThread checkerThread){
            while(checking){
                try {
                    if (checkerThread.currSquare != owner.waitSquare){
                        checkerThread.waitSquare = this;
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            checking = true;
        }

        public synchronized void endCheck(){
            this.checking = false;
            this.owner = null;
            this.notifyAll();
        }

        public String toString(){
            return "(" + this.x + "," + this.y + ")";
        }

    }

    enum Direction{
        NorthEast, SouthEast, SouthWest, NorthWest;

        private static final Direction[] DIRECTIONS = values();
        private static final int SIZE = DIRECTIONS.length;
        private static final Random RANDOM = new Random();

        public static Direction getRandomDirection(){
            return DIRECTIONS[RANDOM.nextInt(SIZE)];
        }
    }


}
