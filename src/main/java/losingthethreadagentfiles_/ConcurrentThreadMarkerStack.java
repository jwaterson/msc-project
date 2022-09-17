package losingthethreadagentfiles_;

import java.util.ArrayList;

public class ConcurrentThreadMarkerStack extends ConcurrentStack<ThreadMarker>{
    public ThreadReference[] toThreadReferenceArray(Thread thread) {
        ArrayList<ThreadReference> list = new ArrayList<>();
        ThreadMarker curr;
        while ((curr = this.pop()) != null) {
            list.add(curr.concatWithThread(thread));
        }
        return list.toArray(ThreadReference[]::new);
    }
}