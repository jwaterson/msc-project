package instrumentation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.*;

/**
 *  A mediator between the user code and application code
 */
public class StackMapMediator {

    public static final ThreadPoolExecutor threadPool;
    public static boolean shutDownInitiated;
    private static final int CAPACITY;
    private static final ConcurrentHashMap<Thread, ConcurrentThreadMarkerStack> threadMap;
    private static final ConcurrentStack<ConcurrentThreadMarkerStack> valStack;
    private static final String CWD;

    static {
        CAPACITY = 16; // arbitrary limit - may be assigned any power of 2
        CWD = System.getProperty("user.dir");
        threadMap = new ConcurrentHashMap<>(CAPACITY << 4, 0.75f,
                CAPACITY << 4);
        // pre-emptively counteracts lazy initialization of the map's internal bin table
        threadMap.put(new Thread(), new ConcurrentThreadMarkerStack());
        valStack = new ConcurrentStack<>();
        for (int i = 0; i < CAPACITY; i++) {
            valStack.push(new ConcurrentThreadMarkerStack());
        }
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() + 1);
        shutDownInitiated = false;
    }

    /**
     * Instantiation disallowed
     */
    private StackMapMediator() {
    }

    /**
     * pop returns null if there are no more queues on stack.
     * If null is returned the key is not valid and cannot be added
     * to ConcurrentHashMap (as per the class's specification).
     *
     * @param th
     * @return
     */
    private static ConcurrentThreadMarkerStack newVal(Thread th) {
        return threadMap.computeIfAbsent(th, k -> valStack.pop());
    }

    /**
     * Used by java agent to get map entry pertaining to
     * currently executing thread.
     *
     * @param th   Thread stored as a key in queueMap
     * @return      value associated with key of value id
     */
    private static ConcurrentThreadMarkerStack getByThreadId(Thread th) {
        ConcurrentThreadMarkerStack q = threadMap.get(th);
        return q != null ? q : newVal(th);
    }

    public static void submitThreadMarker(Thread caller, ThreadMarker tm) {
        try {
            threadPool.execute(() -> getByThreadId(caller).push(tm));
        } catch (Exception ex) {
            if (threadPool.getActiveCount() > 0) {
                System.err.println("Something went wrong during submission!");
            }
            threadPool.shutdown();
        }
    }

    public static void shutdownThreadPool() {
        if (shutDownInitiated) {
            return;
        }
        shutDownInitiated = true;
        threadPool.execute(() -> {
            // if no user thread is alive the program has completed
            while (threadMap.keySet().stream().anyMatch(Thread::isAlive)) {
                try {
                    // check again after sleeping
                    Thread.sleep(10_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            threadPool.shutdown();
            try {
                // give remaining workers 1 sec to complete adding last ThreadMarkers
                if (!threadPool.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
            output();
        });
    }

    /**
     *
     *
     * @return      array of String arrays containing ThreadMarker data and Thread id
     */
    private static ThreadReference[] mapStacksToArray() {
        return threadMap.entrySet().stream()
                .map(m -> m.getValue().toThreadReferenceStringArray(m.getKey()))
                .flatMap(Arrays::stream)
                .sorted(StackMapMediator::compareThreadNanos)
                .toArray(ThreadReference[]::new);
    }

    /**
     * @param curr      A String array containing ThreadMarker information
     * @param other     A String array containing ThreadMarker information
     * @return          int result of comparison
     */
    private static int compareThreadNanos(ThreadReference curr, ThreadReference other) {
        long time1 = Long.parseLong(curr.elements[0]);
        long time2 = Long.parseLong(other.elements[0]);
        return Long.compare(time1, time2);
    }

    private static void output() {
        String dir = CWD + "/losingthethreadoutput/";
        File f;
        if (!((f = new File(dir)).exists())) {
            f.mkdirs();
        }

        try (FileWriter fw = new FileWriter(dir +
                new SimpleDateFormat("yyy.MM.dd.HH.mm.ss").format(new Date()))) {
            fw.write("""
                        __               _                __  __            ________                        __
                       / /   ____  _____(_)___  ____ _   / /_/ /_  ___     /_  __/ /_  ________  ____ _____/ /
                      / /   / __ \\/ ___/ / __ \\/ __ `/  / __/ __ \\/ _ \\     / / / __ \\/ ___/ _ \\/ __ `/ __  /\s
                     / /___/ /_/ (__  ) / / / / /_/ /  / /_/ / / /  __/    / / / / / / /  /  __/ /_/ / /_/ / \s
                    /_____/\\____/____/_/_/ /_/\\__, /   \\__/_/ /_/\\___/    /_/ /_/ /_/_/   \\___/\\__,_/\\__,_/  \s
                                             /____/                                                          \s
                    """);
            for (ThreadReference thref : mapStacksToArray()) {
                fw.write(thref.toString() + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}