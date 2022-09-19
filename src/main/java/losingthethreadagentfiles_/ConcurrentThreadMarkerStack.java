package losingthethreadagentfiles_;

import java.util.ArrayList;

/**
 * A ThreadMarker specific extension of the ConcurrentStack
 * class whose toThreadReferenceArray method enables the
 * creation of ThreadReferences from stack elements and
 * a passed Thread object.
 *
 * @author Josh Waterson
 */
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