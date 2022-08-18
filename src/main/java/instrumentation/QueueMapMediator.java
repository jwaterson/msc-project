package instrumentation;

import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *  A mediator between the user code and application code
 */
public class QueueMapMediator {

    private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<ThreadMarker>> queueMap;

    private static final int CAPACITY;

    private static final ConcurrentStack<ConcurrentLinkedQueue<ThreadMarker>> queueStack;

    static {
        CAPACITY = 16; // arbitrary limit - may be assigned any power of 2
        queueMap = new ConcurrentHashMap<>(CAPACITY << 4, 0.75f, CAPACITY << 4);
        queueStack = new ConcurrentStack<>();
        for (int i = 0; i < CAPACITY; i++) {
            queueStack.push(new ConcurrentLinkedQueue<>());
        }
    }
    /**
     * Instantiation disallowed
     */
    private QueueMapMediator() {
    }

    private static ConcurrentLinkedQueue<ThreadMarker> newQueue(String id) {
        ConcurrentLinkedQueue<ThreadMarker> q = queueStack.pop();
        queueMap.put(id, q);
        return q;
    }

    /**
     * Used by java agent to get map entry pertaining to
     * currently executing thread.
     *
     * @param id    Thread id stored as a key in queueMap
     * @return      value associated with key of value id
     */
    public static ConcurrentLinkedQueue<ThreadMarker> getByThreadId(String id) {
        ConcurrentLinkedQueue<ThreadMarker> q = queueMap.get(id);
        return q != null ? q : newQueue(id);
    }

    /**
     * @return
     */
    public static String[][] output() {
        return queueMap.entrySet().stream()
                .map(m -> m.getValue().stream().map(e -> new String[]{
                                String.valueOf(e.getElements()[0]),
                                String.valueOf(e.getElements()[1]),
                                String.valueOf(e.getElements()[2]),
                                m.getKey()})
                        .toArray(String[][]::new))
                .flatMap(Arrays::stream)
                .sorted(QueueMapMediator::compareThreadNanos)
                .toArray(String[][]::new);
    }

    /**
     * @param curr      A String array containing ThreadMarker information
     * @param other     A String array containing ThreadMarker information
     * @return          int result of comparison
     */
    private static int compareThreadNanos(String[] curr, String[] other) {
        long time1 = Long.parseLong(curr[0]);
        long time2 = Long.parseLong(other[0]);
        return Long.compare(time1, time2);
    }

    public static void printOutput() {
        for (String[] arr : output()) {
            System.out.println(Arrays.toString(arr));
        }
    }

}