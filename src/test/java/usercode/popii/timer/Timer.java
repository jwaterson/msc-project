package usercode.popii.timer;

public class Timer implements Runnable {
    long duration;

    // receives length in seconds
    Timer(long duration) {
        this.duration = duration;
    }

    public void run() {
        try {
            Thread.sleep( duration * 1000 );
        }
        catch( InterruptedException e ) {
            e.printStackTrace();
        }
        System.out.println( "Timer is done!" );
    }
}
