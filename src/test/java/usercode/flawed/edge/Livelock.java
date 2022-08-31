package usercode.flawed.edge;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Simple example of livelock
 * (sourced from <a href="https://www.baeldung.com/java-deadlock-livelock">Baeldung</a>).
 * Losing the Thread requires a program to complete
 * before outputting its results. This program does
 * not terminate and thus, will not allow Losing the
 * Thread to output its result.
 *
 * @author Kamlesh Kumar and Nikolaos Themelis
 */
public class Livelock {

    private Lock lock1 = new ReentrantLock(true);
    private Lock lock2 = new ReentrantLock(true);

    public static void main(String[] args) {
        Livelock livelock = new Livelock();
        new Thread(livelock::operation1, "T1").start();
        new Thread(livelock::operation2, "T2").start();
    }

    public void operation1() {
        while (true) {
            tryLock(lock1, 50);
            System.out.println("lock1 acquired, trying to acquire lock2.");
            try {
                Thread.sleep(50);
            } catch (Exception e) {

            }

            if (tryLock(lock2)) {
                System.out.println("lock2 acquired.");
            } else {
                System.out.println("cannot acquire lock2, releasing lock1.");
                lock1.unlock();
                continue;
            }

            System.out.println("executing first operation.");
            break;
        }
        lock2.unlock();
        lock1.unlock();
    }

    public void operation2() {
        while (true) {
            tryLock(lock2, 50);
            System.out.println("lock2 acquired, trying to acquire lock1.");
            try {
                Thread.sleep(50);
            } catch (Exception e) {

            }

            if (tryLock(lock1)) {
                System.out.println("lock1 acquired.");
            } else {
                System.out.println("cannot acquire lock1, releasing lock2.");
                lock2.unlock();
                continue;
            }

            System.out.println("executing second operation.");
            break;
        }
        lock1.unlock();
        lock2.unlock();
    }

    public void tryLock(Lock lock, long millis) {
        try {
            lock.tryLock(millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean tryLock(Lock lock) {
        return lock.tryLock();
    }
}
