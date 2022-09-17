package usercode.flawed;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;

/**
 *
 */
public class ListAdderRunner {

    static final long START_TIME = System.nanoTime();

    static class ListAdder implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                addToList();
            }
        }
    }

    static final List<Integer> list = new ArrayList<>();

    public static void addToList() {
        synchronized (Thread.currentThread()) { // incorrect synchronization
            list.add(list.size());
        }
    }

    private static int treat(int listSize) {
        StringJoiner sj = new StringJoiner(", ");
        for (int i = 0; i < listSize; i++) {
            sj.add(String.valueOf(i).repeat(i << 2));
        }
        return sj.toString().split(", ").length;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            new Thread(new ListAdder(), "th" + i).start();
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        list.forEach(System.out::println);
    }
}



