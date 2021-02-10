package a1;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.*;

public class fault {
    // Parameters
    public static BufferedImage outputImage;
    public static int width; // args[0]
    public static int height; // args[1]
    public static int threads; // args[2]
    public static int k; // total number of fault lines, args[3]

    public static int faultLine = 0; // current number of drawn fault lines
    public static int max;
    public static int min;

    public static long startTime;
    public static long finishTime;

    public static int[][] coordinate = new int[height][width];
    public static List<HeightAdjustor> heightAdjustors = new ArrayList<>();

    public static void main(String[] args) {
        try {
            if (args.length>0) {
                width = Integer.parseInt(args[0]);
                height = Integer.parseInt(args[1]);
                threads = Integer.parseInt(args[2]);
                k = Integer.parseInt(args[3]);
            }

            // once we know what size we want we can creat an empty image
            outputImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

            startTime = System.currentTimeMillis();
            for (int i=0; i<threads; i++){
                HeightAdjustor heightAdjustor = new HeightAdjustor();
                heightAdjustors.add(heightAdjustor);
            }

            // main thread waits for all threads to finish
            for (HeightAdjustor heightAdjustor : heightAdjustors){
                heightAdjustor.myThread.join();
            }

            calculateRange();
            // map range to color values
            int multi = (255) / (max - min);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    Color color = new Color(0, 0, (coordinate[row][col] - min) * multi);
                    outputImage.setRGB(col, row, color.getRGB());
                }
            }
            finishTime = System.currentTimeMillis();

            System.out.println("width:" + width
                        + "\t height:" + height
                        + "\t k:" + k
                        + "\t threads:" + threads
                        + "\t time:" + (finishTime - startTime) + " ms");

            // Write out the image
            File outputFile = new File("outputimage.png");
            ImageIO.write(outputImage, "png", outputFile);

        } catch (Exception e) {
            System.out.println("ERROR " +e);
            e.printStackTrace();
        }
    }

    /**
     * determine a global max and min
     */
    static void calculateRange(){
        max = coordinate[0][0];
        min = coordinate[0][0];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (coordinate[row][col] > max)  max = coordinate[row][col];
                if (coordinate[row][col] < min)  min = coordinate[row][col];
            }
        }
    }



    static class HeightAdjustor implements Runnable{
        Thread myThread;

        HeightAdjustor(){
            myThread = new Thread(this);
            myThread.start();
        }

        @Override
        public void run() {
            while(faultLine < k) {
                defineFaultLine();
            }
        }

        /**
         * define a fault line and a random height adjustment
         * adjust height field of every point
         */
        void defineFaultLine(){
            // obtain a random height adjustment within [0,10]
            Random rand = new Random();
            int heightAdjustment = rand.nextInt(11);

            // obtain 2 random exit/enter points within ([0,w-1], [0,h-1])
            int x0 = rand.nextInt(width);
            int y0 = rand.nextInt(height);

            int x1 = rand.nextInt(width);
            int y1 = rand.nextInt(height);

            // add height adjustment value to every point on one side of the line
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    int side = (x1 - x0) * (row - y0) - (col - x0) * (y1 - y0);
                    // add height adjustment to every point's height on left side
                    if (side > 0) {
                        synchronized (this) {
                            coordinate[row][col] += heightAdjustment;
                        }
                    }
                }
            }
            faultLine++;
        }



    }
}