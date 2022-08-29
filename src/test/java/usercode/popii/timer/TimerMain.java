package usercode.popii.timer;

import java.util.*;


public class TimerMain {

    public static void main(String[] args) {
        Thread thread = null;
        long duration;

        while( true ) {
            duration = new Scanner(System.in).nextLong();
            if (duration < 0) {
                return;
            }
            if(thread != null && thread.isAlive()) {
                System.out.println( "Timer still running!" );
                continue;
            }
            thread = new Thread(new Timer(duration));
            thread.start();
        }
    }

}