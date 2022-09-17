package losingthethreadagentfiles_;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentThreadMarkerStackTest {

    private ConcurrentThreadMarkerStack emptyStack;
    private ConcurrentThreadMarkerStack singleElementStack;
    private ConcurrentThreadMarkerStack duplicateElementDataStack;
    private ConcurrentThreadMarkerStack largeStack;
    private ThreadMarker tm1;
    private ThreadReference[] threadRefArr1;
    private ThreadReference[] threadRefArr2;
    private ThreadReference[] threadRefArr3;
    private Thread mainThread;

    @BeforeEach
    void setUp() {
        // reference to current thread
        mainThread = Thread.currentThread();

        // stacks
        emptyStack = new ConcurrentThreadMarkerStack();
        singleElementStack = new ConcurrentThreadMarkerStack();
        duplicateElementDataStack = new ConcurrentThreadMarkerStack();
        largeStack = new ConcurrentThreadMarkerStack();

        // thread markers
        tm1 = new ThreadMarker(-1, -1, "fakeClass");

        // thead references
        threadRefArr1 = new ThreadReference[]{new ThreadReference(tm1, mainThread)};
        threadRefArr2 = new ThreadReference[]{
                new ThreadReference(tm1, mainThread),
                // same data, different object
                new ThreadReference(new ThreadMarker(-1, -1,
                        "fakeClass"), mainThread)
        };

        // prep
        singleElementStack.push(tm1);

        duplicateElementDataStack.push(tm1);
        duplicateElementDataStack.push(tm1);

        ArrayList<ThreadMarker> list = new ArrayList<>();
        // Short's MAX_VALUE (32,767) is the max value
        for (int i = 1; i <= Short.MAX_VALUE; i++) {
            ThreadMarker tempTm = new ThreadMarker(i, i, "fakeClass");
            largeStack.push(tempTm);
            list.add(0, tempTm);
        }
        threadRefArr3 = list.stream()
                .map(i -> new ThreadReference(i, mainThread))
                .toArray(ThreadReference[]::new);

    }

    @AfterEach
    void tearDown() {
        emptyStack = null;
        singleElementStack = null;
        duplicateElementDataStack = null;
        tm1 = null;
        threadRefArr1 = null;
        threadRefArr2 = null;
    }

    @Test
    void toThreadReferenceArrayPassing() {
        assertAll(
                // empty stack creates empty ThreadReference
                () -> assertArrayEquals(new ThreadReference[]{},
                        emptyStack.toThreadReferenceArray(mainThread)),
                // single basic stack element is represented
                () -> assertArrayEquals(threadRefArr1,
                        singleElementStack.toThreadReferenceArray(mainThread)),
                // duplicated lineNum, thread and className data is represented in each instance
                () -> assertArrayEquals(threadRefArr2,
                        duplicateElementDataStack.toThreadReferenceArray(mainThread)),
                // max possible number of elements in stack
                () -> assertArrayEquals(threadRefArr3,
                        largeStack.toThreadReferenceArray(mainThread))
        );
    }

    @Test
    void toThreadReferenceArrayFailing() {
        assertAll(
                // empty stack is not represented as null
                () -> assertNotEquals(null,
                        emptyStack.toThreadReferenceArray(mainThread))

        );
    }
}