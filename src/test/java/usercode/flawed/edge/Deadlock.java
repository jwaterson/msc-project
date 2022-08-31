package usercode.flawed.edge;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple example of livelock.
 * Losing the Thread requires a program to complete
 * before outputting its results. This program does
 * not terminate and thus, will not allow Losing the
 * Thread to output its result.
 */
public class Deadlock {
    static List<Integer> a;
    static List<Integer> b;
    static {
        a = new ArrayList<>(List.of(1, 2, 3));
        b = new ArrayList<>(List.of(-1, -2, -3));
    }

    public static void main(String[] args) {
        Thread th1 = new Thread(() -> updateList(a), "th1");
        Thread th2 = new Thread(() -> updateList(b), "th2");

        th1.start();
        th2.start();
    }

    private static void updateList(List<Integer> list) {
        List<Integer> other = list.equals(a) ? b : a;
        synchronized (other) {
            System.out.printf("%s locked other list\n",
                    Thread.currentThread().getName());
            synchronized (list) {
                System.out.printf("%s locked this list\n",
                        Thread.currentThread().getName());
                list.add(other.get(other.size() - 1));
            }
        }
        System.out.println(list);
    }
}
