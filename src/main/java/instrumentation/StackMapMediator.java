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
 *
 * @author Josh Waterson
 */
public class StackMapMediator {

    public static final ThreadPoolExecutor threadPool;
    public static boolean shutDownInitiated;

    private static final int CAPACITY;
    private static final ConcurrentHashMap<Thread, ConcurrentThreadMarkerStack> threadMap;
    private static final ConcurrentStack<ConcurrentThreadMarkerStack> valStack;
    private static final String CWD;

    static {
        CAPACITY = 1024; // arbitrary limit
        CWD = System.getProperty("user.dir");
        threadMap = new ConcurrentHashMap<>();
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
     * pop returns null if there are no more elements on stack.
     * If null is returned the key is not valid and cannot be added
     * to the ConcurrentHashMap (as per the class's specification).
     *
     * @param th -  the user thread to be stored as a key
     *           on the threadMap
     * @return      the new stack value to which the passed Thread maps
     */
    private static ConcurrentThreadMarkerStack newVal(Thread th) {
        try {
            return threadMap.computeIfAbsent(th, k -> valStack.pop());
        } catch (NullPointerException e) {
            throw new RuntimeException("Thread count exceeded 1000 (limit).");
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
     * Adds a new ThreadMarker to the threadMap.
     *
     * @param caller    The key Thread
     * @param tm        The new ThreadMarker object to be added
     */
    public static void submitThreadMarker(Thread caller, ThreadMarker tm) {
//        try {
//            threadPool.execute(() -> {
//                ConcurrentThreadMarkerStack stack = threadMap.get(caller);
//                (stack != null ? stack : newVal(caller)).push(tm);
//            });
//        } catch (Exception ex) {
//            if (threadPool.getActiveCount() > 0) {
//                System.err.println("Something went wrong during submission!");
//            }
//            threadPool.shutdown();
//        }
        ConcurrentThreadMarkerStack stack = threadMap.get(caller);
        (stack != null ? stack : newVal(caller)).push(tm);
    }

    /**
     * Iterates through threadMap's keySet, calling
     * join on each key Thread.
     */
    public static void joinUserThreads() {
        for (Thread th : threadMap.keySet()) {
            if (!(th.equals(Thread.currentThread()))) {
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        output();
    }

    /**
     *  maps the threadMap's entries to a flattened array
     *  of ThreadReferences.
     *
     * @return      array of ThreadReferences
     */
    private static ThreadReference[] threadMapEntriesToArray() {
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

    /**
     * Outputs sequential list of ThreadReferences to a new output
     * file within the /losingthethread/ directory.
     */
    private static void output() {
        String dir = CWD + "/losingthethreadoutput/";
        File f;
        if (!((f = new File(dir)).exists())) {
            f.mkdirs();
        }

        try (FileWriter fw = new FileWriter(dir +
                new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + ".txt")) {
            fw.write("""
                        __               _                __  __            ________                        __
                       / /   ____  _____(_)___  ____ _   / /_/ /_  ___     /_  __/ /_  ________  ____ _____/ /
                      / /   / __ \\/ ___/ / __ \\/ __ `/  / __/ __ \\/ _ \\     / / / __ \\/ ___/ _ \\/ __ `/ __  /\s
                     / /___/ /_/ (__  ) / / / / /_/ /  / /_/ / / /  __/    / / / / / / /  /  __/ /_/ / /_/ / \s
                    /_____/\\____/____/_/_/ /_/\\__, /   \\__/_/ /_/\\___/    /_/ /_/ /_/_/   \\___/\\__,_/\\__,_/  \s
                                             /____/                                                          \s
                    """);
            for (ThreadReference thref : threadMapEntriesToArray()) {
                fw.write(thref.toString() + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}