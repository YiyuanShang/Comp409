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
    public static int width;
    public static int height;
    public static int threads;
    public static int k;

    public static int faultLine = 0;
    public static int max;
    public static int min;
    static boolean running = true;

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

            long startTime = System.currentTimeMillis();
            for (int i=0; i<threads; i++){
                HeightAdjustor heightAdjustor = new HeightAdjustor();
                heightAdjustors.add(heightAdjustor);
            }
            while(faultLine <k){
                Thread.sleep(10);
            }
            running = false;
            for (HeightAdjustor heightAdjustor: heightAdjustors){
                heightAdjustor.myThread.join();
            }

            long finishTime = System.currentTimeMillis();
            System.out.println("threads:" + threads + "\ttime:" + (finishTime - startTime) + " ms");

            max = coordinate[0][0];
            min = coordinate[0][0];
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    if (coordinate[row][col] > max)  max = coordinate[row][col];
                    if (coordinate[row][col] < min)  min = coordinate[row][col];
                }
            }
            int multi = (255)/(max-min);
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < width; col++) {
                    Color color = new Color(0,0,(coordinate[row][col]-min)*multi);
                    outputImage.setRGB(col, row, color.getRGB());
                }
            }

            // Write out the image
            File outputFile = new File("outputimage.png");
            ImageIO.write(outputImage, "png", outputFile);

        } catch (Exception e) {
            System.out.println("ERROR " +e);
            e.printStackTrace();
        }
    }

    // threads to adjust the height of points
    static class HeightAdjustor implements Runnable{
        Thread myThread;

        HeightAdjustor(){
            myThread = new Thread(this);
//            System.out.println("thread " + myThread.getId() + " created");
            myThread.start();
        }

        @Override
        public void run() {
            try {
                while(running) {
                    adjustHeight();
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized void adjustHeight(){
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
                    // skip off 2 grid points
                    if ((col == x0 && row == y0) || (col == x1 && row == y1)) {
                        continue;
                    }
                    int side = (x1 - x0) * (row - y0) - (col - x0) * (y1 - y0);
                    // add height adjustment to every point's height on left side
                    if (side > 0) {

                        coordinate[row][col] += heightAdjustment;
                        if (coordinate[row][col] > max) {
                            max = coordinate[row][col];
                        }
                        if (coordinate[row][col] < min) {
                            min = coordinate[row][col];
                        }
                    }


                }

            }
            faultLine++;

        }

        }
    }

