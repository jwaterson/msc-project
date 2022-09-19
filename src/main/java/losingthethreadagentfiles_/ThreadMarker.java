package losingthethreadagentfiles_;


/**
 * A simple class that stores thread data whose concatWithThread
 * method, outputs ThreadReferences
 *
 * @author Josh Waterson
 */
public class ThreadMarker {
    private final Object[] elements;

    public ThreadMarker(long time, int lineNum, String className) {
        elements = new Object[]{time, lineNum, className};
    }

    public Object[] getElements() {
        return elements;
    }

    ThreadReference concatWithThread(Thread thread) {
        return new ThreadReference(this, thread);
    }
}