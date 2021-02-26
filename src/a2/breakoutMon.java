package a2;

import java.util.ArrayList;
import java.util.List;


public class breakoutMon {
    static long n = 10 * 1000; // running period
    static int k = 10; // sleeping time before a student makes an attempt
    static int w = 20; // sleeping time after a student entered the room
    static Room room = new Room();
    static boolean running = true;

    public static void main(String[] args) {
        // use default settings unless given command line arguments
        if (args.length>0){
            n = Long.valueOf(args[0]);
            k = Integer.valueOf(args[1]);
            w = Integer.valueOf(args[2]);
        }

        long startTime = System.currentTimeMillis();
        // start 12 threads
        List<Student> studentList = new ArrayList<>();
        for (int i=0; i<4; i++){
            studentList.add(new Student(Faculty.ART, i+1));
        }
        for (int i=4; i<8; i++){
            studentList.add(new Student(Faculty.SCI, i+1));
        }
        for (int i=8; i<12; i++){
            studentList.add(new Student(Faculty.ENG, i+1));
        }

        while (System.currentTimeMillis() - startTime < n);
        running = false;
        // main thread waits for all threads to finish
        for (Student student: studentList){
            try {
                student.myThread.join();
            } catch (InterruptedException e) {
            }
        }
//        System.out.println("[DEBUG] Main thread: finished");
    }

    /**
     * monitor class
     */
    static class Room{
        private static Faculty owner = Faculty.NOFACULTY;
        private static int studentNum = 0;

        public synchronized void studentArrived(Student student){
            // if room is empty or owner is from the same faculty
            // then student can enter
            // else wait until room is free
            while (!(owner == student.faculty || studentNum==0)) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                }
            }

            owner = student.faculty;
            studentNum++;
//            System.out.println("[DEBUG] enter:" + student.studentId + "(" + student.faculty + ") entered room ["+ studentNum + "]");
            System.out.println("Owner:" + owner);

            // student sleeps for w-10w ms after entering the room
            try {
                Thread.sleep((long)Math.random()*(10*w - w + 1) + w);
            } catch (InterruptedException e) {
            }
        }

        public synchronized void studentLeft(Student student){
            studentNum--;
//            System.out.println("[DEBUG] leave:" + student.studentId + "(" + student.faculty + ") left room [" + studentNum + "]");
            if (studentNum == 0){
                owner = Faculty.NOFACULTY;
                System.out.println("Owner:" + owner);
            }
            this.notifyAll();

        }

    }

    static class Student implements Runnable{
        Thread myThread;
        private Faculty faculty;
        private int studentId;


        public Student(Faculty faculty, int studentId){
            this.faculty = faculty;
            this.studentId = studentId;
            myThread = new Thread(this);
            myThread.start();
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
