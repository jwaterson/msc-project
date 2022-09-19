package losingthethreadagentfiles_.agent;

import java.lang.instrument.Instrumentation;


/**
 * Java agent used to intercept classloading in order to transform classes
 * with the ThreadRecorder class.
 *
 * @author Josh Waterson
 */
public class Agent {

    static final long START_TIME = System.nanoTime();

    public static void premain(String args, Instrumentation instrumentation) {
        instrumentation.addTransformer(new ThreadRecorder());
    }

}

