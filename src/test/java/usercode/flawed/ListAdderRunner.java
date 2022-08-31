package usercode.flawed;

import instrumentation.StackMapMediator;
import instrumentation.ThreadMarker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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



