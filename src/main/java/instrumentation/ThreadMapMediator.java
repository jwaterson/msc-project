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
public class ThreadMapMediator {
    static final int CAPACITY;
    private static final ConcurrentHashMap<Thread, ConcurrentThreadMarkerStack> threadMap;
    private static final ConcurrentStack<ConcurrentThreadMarkerStack> valStack;
    private static final String CWD;

    static {
        CAPACITY = 1024; // arbitrary limit
        CWD = System.getProperty("user.dir");
        threadMap = new ConcurrentHashMap<>(CAPACITY);
        valStack = new ConcurrentStack<>();
        for (int i = 0; i < CAPACITY; i++) {
            valStack.push(new ConcurrentThreadMarkerStack());
        }
    }

    /**
     * Instantiation disallowed
     */
    private ThreadMapMediator() {
    }

    /**
     * pop returns null if there are no more elements on stack.
     * If null is returned the key is not valid and cannot be added
     * to the ConcurrentHashMap (as per the class's specification).
     *
     * @param th -      the user thread to be stored as a key
     *                  on the threadMap
     * @return          the new stack value to which the passed Thread maps
     */
    private static ConcurrentThreadMarkerStack newVal(Thread th) {
        try {
            return threadMap.computeIfAbsent(th, k -> valStack.pop());
        } catch (NullPointerException e) {
            throw new RuntimeException("Thread count exceeded limit of " + CAPACITY);
        }
    }

    /**
     * Adds a new ThreadMarker to the threadMap.
     *
     * @param caller    The key Thread
     * @param tm        The new ThreadMarker object to be added
     */
    public static void submitThreadMarker(Thread caller, ThreadMarker tm) {
        ConcurrentThreadMarkerStack stack = threadMap.get(caller);
        (stack != null ? stack : newVal(caller)).push(tm);

    }

    /**
     * Iterates through threadMap's keySet, calling
     * join on each key Thread.
     */
    public static void terminate() {
        Thread main = Thread.currentThread();
        boolean unjoined = threadMap.keySet().stream()
                .anyMatch(th -> th.isAlive() && !(th.equals(main)));
        for (Thread th : threadMap.keySet()) {
            if (!(th.equals(main))) {
                try {
                    th.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        ThreadMapOutputWriter.output(unjoined);
    }

    static class ThreadMapOutputWriter {
        /**
         *  maps the threadMap's entries to a flattened array
         *  of ThreadReferences.
         *
         * @return          array of ThreadReferences
         */
        private static ThreadReference[] threadMapEntriesToArray() {
            return threadMap.entrySet().stream()
                    .map(m -> m.getValue().toThreadReferenceArray(m.getKey()))
                    .flatMap(Arrays::stream)
                    .sorted(ThreadMapOutputWriter::compareThreadNanos)
                    .toArray(ThreadReference[]::new);
        }

        /**
         * @param curr      A String array containing ThreadMarker information
         * @param other     A String array containing ThreadMarker information
         * @return          int result of comparison
         */
        private static int compareThreadNanos(ThreadReference curr, ThreadReference other) {
            long time1 = Long.parseLong(curr.getElements()[0].toString());
            long time2 = Long.parseLong(other.getElements()[0].toString());
            return Long.compare(time1, time2);
        }

        /**
         * Outputs sequential list of ThreadReferences to a new output
         * file within the /losingthethread/ directory.
         *
         * @param unjoined  indicates whether any live threads were detected
         */
        private static void output(boolean unjoined) {
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

                if (unjoined) {
                    fw.write("""
                        Warning:
                        One or more threads were detected as not having been joined upon completion of the program. 
                        These were joined on your behalf.

                        """);
                }

                for (ThreadReference thref : threadMapEntriesToArray()) {
                    fw.write(thref.toString() + "\n");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}