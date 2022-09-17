package losingthethreadagentfiles_;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreadReferenceTest {

    private Thread mainThread = Thread.currentThread();

    private ThreadReference threadReference1;
    private ThreadReference threadReference2;
    private ThreadReference threadReference3;
    private ThreadReference threadReference4;
    private ThreadReference threadReference5;

    private ThreadMarker threadMarker1;

    @BeforeEach
    void setUp() {
        threadMarker1 = new ThreadMarker(Long.MAX_VALUE, Integer.MAX_VALUE,
                "made/up/Class");
        ThreadMarker threadMarker2 = new ThreadMarker(Long.MAX_VALUE, Integer.MAX_VALUE,
                "made/up/Class");
        ThreadMarker threadMarker3 = new ThreadMarker(1, 1,
                "made/up/Class");
        ThreadMarker threadMarker4 = new ThreadMarker(42, Integer.MAX_VALUE,
                "made/up/Class");

        threadReference1 = new ThreadReference(threadMarker1, mainThread);
        threadReference2 = new ThreadReference(threadMarker2, mainThread);
        threadReference3 = new ThreadReference(threadMarker3, mainThread);
        threadReference4 = new ThreadReference(threadMarker4, mainThread);
        threadReference5 = new ThreadReference(threadMarker1, new Thread());

    }

    @AfterEach
    void tearDown() {
        threadReference1 = null;
        threadReference2 = null;
        threadReference3 = null;
    }

    @Test
    void testEquals() {
        assertAll(
                // same objects
                () -> assertEquals(threadReference1, threadReference1),
                // all elements same, different objects
                () -> assertEquals(threadReference1, threadReference2),
                // different elements, different objects
                () -> assertNotEquals(threadReference2, threadReference3),
                // one null
                () -> assertNotEquals(threadReference1, null),
                // different objects
                () -> assertNotEquals(threadReference1, threadMarker1),
                // one ThreadMarker element differs
                () -> assertNotEquals(threadReference1, threadReference4),
                // threads differ
                () -> assertNotEquals(threadReference1, threadReference5)
        );
    }
}