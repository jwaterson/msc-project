package instrumentation;

import java.util.ArrayList;

public class ConcurrentThreadMarkerStack extends ConcurrentStack<ThreadMarker>{
    public String[][] toThreadReferenceStringArray(Thread thread) {
        ArrayList<String[]> list = new ArrayList<>();
        ThreadMarker curr;
        while ((curr = this.pop()) != null) {
            list.add(curr.concatWithThread(thread));
        }
        return list.toArray(String[][]::new);
    }
}