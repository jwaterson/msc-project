package uats;

import java.util.ArrayList;
import java.util.Random;




/*
 * Expected output: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
 *
 * You try running the program and get the following
 * unexpected output: [0, 0, 2, 3]
 *
 * Task:    Describe how this might have happened by giving a brief
 *          summarising account of how you think the program executed.
 *
 *          Try to list any mistakes/semantic misapprehensions you
 *          encounter in the code.
 */

public class Main {

    static final ArrayList<Integer> arrayList = new ArrayList<>();

    static class BlinkingAdder implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(new Random().nextInt(0, 2));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            addElement(arrayList.size());
        }

        synchronized private void addElement(int size) {
            arrayList.add(size);
        }
    }

    private static void addToList() {
        for (int i = 0; i < 10; i++) {
            Thread th = new Thread(new BlinkingAdder());
            th.start();
        }
    }

    public static void main(String[] args) {
        addToList();
        System.out.println(arrayList);
    }
}
