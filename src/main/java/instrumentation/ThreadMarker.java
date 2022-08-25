package instrumentation;

public class ThreadMarker {
    final Object[] elements;
    public ThreadMarker(long time, int lineNum, String className) {
        elements = new Object[]{time, lineNum, className};
    }

    public Object[] getElements() {
        return elements;
    }

    public String[] concatWithThread(Thread thread) {
        return new String[]{
                String.valueOf(elements[0]),
                String.valueOf(elements[1]),
                String.valueOf(elements[2]),
                String.valueOf(thread)};
    }
}