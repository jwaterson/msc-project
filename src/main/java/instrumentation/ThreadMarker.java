package instrumentation;


import java.util.Arrays;
import java.util.Objects;

public class ThreadMarker {

    final Object[] elements;
    public ThreadMarker(long time, int lineNum, String className) {
        elements = new Object[]{time, lineNum, className};
    }

    public Object[] getElements() {
        return elements;
    }

    public ThreadReference concatWithThread(Thread thread) {
        return new ThreadReference(this, thread);
    }
}