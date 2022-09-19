package losingthethreadagentfiles_;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static losingthethreadagentfiles_.ThreadMapMediator.CAPACITY;
import static org.junit.jupiter.api.Assertions.*;

class ThreadMapMediatorTest {

    private long START = System.nanoTime();
    private Field f;
    private ConcurrentHashMap<Thread, ConcurrentThreadMarkerStack> map;

    @BeforeEach
    void setUp() {
        try {
            f = ThreadMapMediator.class.getDeclaredField("threadMap");
            f.setAccessible(true);
            map = (ConcurrentHashMap<Thread, ConcurrentThreadMarkerStack>)
                            f.get(ThreadMapMediator.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        try {
            f = ThreadMapMediator.class.getDeclaredField("valStack");
            f.setAccessible(true);
            ConcurrentStack<ConcurrentThreadMarkerStack> stack = (ConcurrentStack<ConcurrentThreadMarkerStack>)
                    f.get(ThreadMapMediator.class);
            // empty the stack
            while (stack.pop() != null) {
            }
            // refresh the valStack for next test adding to threadMap
            for (int i = 0; i < CAPACITY; i++) {
                stack.push(new ConcurrentThreadMarkerStack());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        // refresh the threadMap for next test
        for (Thread k : map.keySet()) {
            map.remove(k);
        }
        f = null;
        map = null;
    }

    @Test
    void submitThreadMarkerOverfullException() {
        assertThrows(NullPointerException.class,
                () -> {
                    for (int i = 1; i <= CAPACITY; i++) {
                        ThreadMapMediator.submitThreadMarker(new Thread(),
                                new ThreadMarker(-1, i,
                                        "Whatever$Subclass"));
                    }
                    // the straw that breaks the camel's back
                    ThreadMapMediator.submitThreadMarker(new Thread(),
                        new ThreadMarker(System.nanoTime(), 1,
                            "Whatever$Subclass"));
                });
    }

    @Test
    void submitThreadMarkerLineNumberTooLargeException() {
        assertThrows(UnsupportedOperationException.class,
                //
                () -> ThreadMapMediator.submitThreadMarker(new Thread(),
                        new ThreadMarker(System.nanoTime(), Short.MAX_VALUE + 1,
                                "Whatever$Subclass")));
    }

    @Test
    void submitThreadMarkerClassNameTooLongException() {
        assertThrows(IllegalArgumentException.class,
                //
                () -> ThreadMapMediator.submitThreadMarker(new Thread(),
                        new ThreadMarker(System.nanoTime(), Short.MAX_VALUE,
                                "_".repeat(Short.MAX_VALUE * 2 + 2))));
    }

    @Test
    void submitThreadMarkerCapacityPassing() {
        for (int i = 1; i <= CAPACITY; i++) {
            ThreadMapMediator.submitThreadMarker(new Thread(),
                    new ThreadMarker((long) i << 2, i,
                            "Whatever$Subclass"));
        }
        assertEquals(1024, map.mappingCount());
    }

    @Test
    void submitThreadMarkerUnevenThreadMarkerDistribution() {
        for (int i = 1; i <= CAPACITY; i++) {
            Thread th = new Thread(() -> {
                int end = ThreadLocalRandom.current().nextInt(2, 1000);
                for (int j = 1; j < end; j++) {
                    ThreadMapMediator.submitThreadMarker(Thread.currentThread(),
                            new ThreadMarker((long) j << 2, j,
                                    "Whatever$Subclass"));
                }
            });
            th.start();

        }
        // wait until all threads have finished
        while (map.keySet().stream().anyMatch(Thread::isAlive)) {
        }

        assertEquals(CAPACITY, map.mappingCount());
    }

}