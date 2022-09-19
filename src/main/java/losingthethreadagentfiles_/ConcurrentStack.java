package losingthethreadagentfiles_;

import java.util.concurrent.atomic.AtomicReference;


/**
 * A stack structure authored by Brian Goetz and Tim Peierls
 * which uses CAS to enable thread-safe concurrent access
 *
 * @param <E>   type contained in Stack
 * @author      Brian Goetz and Tim Peierls
 */
public class ConcurrentStack <E> {

    AtomicReference<Node<E>> top = new AtomicReference<>();

    public void push(E item) {
        Node<E> newHead = new Node<>(item);
        Node<E> oldHead;
        do {
            oldHead = top.get();
            newHead.next = oldHead;
        } while (!top.compareAndSet(oldHead, newHead));
    }

    public E pop() {
        Node<E> oldHead;
        Node<E> newHead;
        do {
            oldHead = top.get();
            if (oldHead == null)
                return null;
            newHead = oldHead.next;
        } while (!top.compareAndSet(oldHead, newHead));
        return oldHead.item;
    }

    private static class Node <E> {
        public final E item;
        public Node<E> next;
        public Node(E item) {
            this.item = item;
        }
    }

}