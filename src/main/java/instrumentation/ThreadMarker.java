package instrumentation;

public class ThreadMarker {
    private final Object[] elements;

    public ThreadMarker(long time, int lineNum, String className) {
        elements = new Object[]{time, lineNum, className};
    }

    public Object[] getElements() {
        return elements;
    }
}