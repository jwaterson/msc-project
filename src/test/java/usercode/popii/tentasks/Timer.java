package usercode.popii.tentasks;

public class Timer implements Runnable {
    static String finished_tasks = "";
    int task_number;
    long length;

    // receives length in seconds
    Timer(long length, int task_number) {

        this.length = length;
        this.task_number = task_number;
    }

    public void run() {
        try {
            Thread.sleep( length );
        }
        catch( InterruptedException e ) {
            e.printStackTrace();
        }
        finished_tasks += this.task_number + " ";
    }
}