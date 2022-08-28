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

class ThreadReference {
    String[] elements;
    ThreadReference(ThreadMarker tm, Thread th) {
        elements = new String[]{
                String.valueOf(tm.elements[0]),
                String.valueOf(tm.elements[1]),
                String.valueOf(tm.elements[2]),
                String.valueOf(th),
                String.valueOf(th.hashCode())
        };
    }

    @Override
    public String toString() {
        return Arrays.toString(this.elements);
    }
}