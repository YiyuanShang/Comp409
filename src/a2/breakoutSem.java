package a2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class breakoutSem {
    public static long n = 10 * 1000; // running period for all threads
    public static int k = 20; // sleeping time before student attempts to enter room
    public static int w = 30; // sleeping time when student is in room
    public static boolean running = true;


    public static void main(String[] args) {
        Room room = new Room();
        long startTime = System.currentTimeMillis();

        Thread thread[] = new Thread[12];
        for (int i=0; i<4; i++){
            thread[i] = new Thread(new Student(Faculty.ART, i+1, room), "Thread " + i+1);
        }
        for (int i=4; i<8; i++){
            thread[i] = new Thread(new Student(Faculty.SCI, i+1, room), "Thread " + i+1);
        }
        for (int i=8; i<12; i++){
            thread[i] = new Thread(new Student(Faculty.ENG, i+1, room), "Thread " + i+1);
        }
        for (int i=0; i<12; i++){
            thread[i].start();
        }

        while (System.currentTimeMillis() - startTime < n);
        running = false;
        for (int i=0; i<12; i++){
            try {
                thread[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[DEBUG] Main thread: finished");
    }

    static class Room{
        private final Semaphore semaphore;
        private static Faculty owner;
        private static int studentNum;

        public Room(){
            semaphore = new Semaphore(1);
            owner = Faculty.NOFACULTY;
            studentNum = 0;
        }

        public void studentArrived(Student student){
            try{
                if (student.faculty == owner || studentNum==0){
                    // only one student is permitted to enter the room at a time
                    semaphore.acquire();

                    // student entered the room
                    student.inRoom = true;
                    owner = student.faculty;
                    studentNum++;
                    System.out.println("[DEBUG] enter:" + student.studentId + "(" + student.faculty + ") entered room ["+ studentNum + "]");
                    System.out.println("Owner:" + owner);

                    // allow other students to make attempt
                    semaphore.release();

                    // entered student sleeps for w-10w ms
                    Thread.sleep((long)Math.random()*(10*w - w + 1) + w);
                }
            } catch (InterruptedException e) {
            }
        }

        public void studentLeft(Student student){
            // if the student entered the room
            if (student.inRoom) {
                studentNum--;
                System.out.println("[DEBUG] leave:" + student.studentId + "(" + student.faculty + ") left room [" + studentNum + "]");
                if (studentNum == 0) {
                    owner = Faculty.NOFACULTY;
                    System.out.println("EMPTY");
                }
                student.inRoom = false;
            }
        }

    }

    static class Student implements Runnable{
        private Faculty faculty;
        private int studentId;
        private Room room;
        private boolean inRoom;

        public Student(Faculty faculty, int studentId, Room room){
            this.faculty = faculty;
            this.studentId = studentId;
            this.inRoom = false;
            this.room = room;
        }

        @Override
        public void run() {
            while (running){
                try {
                    // student sleeps for k-10k ms
                    Thread.sleep((long) (Math.random() * (10*k - k + 1) + k));

                    // student attempts to enter room
                    room.studentArrived(this);
                    room.studentLeft(this);
                } catch (InterruptedException e) {
                }
            }
        }

    }

    enum Faculty{
        ART, SCI, ENG, NOFACULTY
    }
}
