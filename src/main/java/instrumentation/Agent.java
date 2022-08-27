package instrumentation;

import java.lang.instrument.Instrumentation;


/**
 * Agent class
 *
 * @author Josh Waterson
 */
public class Agent {

    static final long START_TIME = System.nanoTime();

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ThreadObserver());
    }

}

