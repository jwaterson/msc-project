package instrumentation;

import java.util.Arrays;

public class ThreadReference {
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