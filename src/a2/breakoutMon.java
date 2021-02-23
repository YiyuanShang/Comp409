package a2;

import java.util.ArrayList;
import java.util.List;

import static a2.breakoutMon.Room.*;

public class breakoutMon {
    static long n = 10 * 1000;
    static int k = 20;
    static int w = 20;
    static Room room = new Room();
    static boolean running = true;


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        List<Student> studentList = new ArrayList<>();


        studentList.add(new Student(Faculty.ART, 1));
        studentList.add(new Student(Faculty.ART, 2));
        studentList.add(new Student(Faculty.ART, 3));
        studentList.add(new Student(Faculty.ART, 4));

        studentList.add(new Student(Faculty.SCI, 5));
        studentList.add(new Student(Faculty.SCI, 6));
        studentList.add(new Student(Faculty.SCI, 7));
        studentList.add(new Student(Faculty.SCI, 8));

        studentList.add(new Student(Faculty.ENG, 9));
        studentList.add(new Student(Faculty.ENG,10));
        studentList.add(new Student(Faculty.ENG, 11));
        studentList.add(new Student(Faculty.ENG, 12));

//        for (Student student:studentList){
//            student.myThread.start();
//        }
        while (System.currentTimeMillis() - startTime < n);
        running = false;
        for (Student student: studentList){
            try {
                student.myThread.interrupt();
                student.myThread.join();
            } catch (InterruptedException e) {
            }
        }
        System.out.println("[DEBUG] Main thread: finished");



    }

    static class Room{
        private static Faculty owner = Faculty.NOFACULTY;
        private static int studentNum = 0;

        public synchronized void studentArrived(Student student){
            while (!(owner == student.faculty || studentNum==0)) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            owner = student.faculty;
            studentNum++;
            System.out.println("[DEBUG] enter:" + student.studentId + "(" + student.faculty + ") entered room ["+ studentNum + "]");
            System.out.println("Owner:" + owner);
            try {
                Thread.sleep((long)Math.random()*(10*w - w + 1) + w);
            } catch (InterruptedException e) {
            }
        }

        public synchronized void studentLeft(Student student){
            studentNum--;
            System.out.println("[DEBUG] leave:" + student.studentId + "(" + student.faculty + ") left room [" + studentNum + "]");
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
                    Thread.sleep((long) (Math.random() * (10*k - k + 1) + k));
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
