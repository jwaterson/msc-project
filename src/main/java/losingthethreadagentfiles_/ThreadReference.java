package losingthethreadagentfiles_;

import java.util.Arrays;
import java.util.stream.Stream;


public class ThreadReference {
    private final Object[] elements;

    ThreadReference(ThreadMarker tm, Thread th) {
        elements = Stream.concat(
                Arrays.stream(tm.getElements()),
                Arrays.stream(new Object[]{th.toString(),
                        th.hashCode()})
            ).toArray();
    }

    public Object[] getElements() {
        return elements;
    }

    @Override
    public String toString() {
        return Arrays.toString(this.elements);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ThreadReference)) {
            return false;
        }
        return Arrays.equals(this.elements,
                ((ThreadReference) o).elements);
    }
}