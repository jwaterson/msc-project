package instrumentation;

import java.util.ArrayList;

public class ConcurrentThreadMarkerStack extends ConcurrentStack<ThreadMarker>{
    public ThreadReference[] toThreadReferenceStringArray(Thread thread) {
        ArrayList<ThreadReference> list = new ArrayList<>();
        ThreadMarker curr;
        while ((curr = this.pop()) != null) {
            list.add(curr.concatWithThread(thread));
        }
        return list.toArray(ThreadReference[]::new);
    }
}