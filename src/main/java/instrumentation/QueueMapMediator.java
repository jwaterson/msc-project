package instrumentation;

import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *  A mediator between the user code and application
 */
public class QueueMapMediator {

    /**
     * Each queue's head is the available name after which both the
     * thread was named and the variable was named in the usercode.
     */
    private static final Map<String, Queue<ThreadMarker>> queueMap;

    private static final int CAPACITY;

    private static final ConcurrentStack<Queue<ThreadMarker>> queueStack;

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

    private static Queue<ThreadMarker> newQueue(String id) {
        Queue<ThreadMarker> q = new ConcurrentLinkedQueue<>();
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
    public static Queue<ThreadMarker> getByThreadId(String id) {
        Queue<ThreadMarker> q = queueMap.get(id);
        return q != null ? q : newQueue(id);
    }

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




}