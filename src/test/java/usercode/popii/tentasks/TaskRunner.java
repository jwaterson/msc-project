package usercode.popii.tentasks;

import java.util.Scanner;

public class TaskRunner {

    public static void main(String[] args) {

        Timer timer = new Timer(1,-1);
        Thread[] threads = new Thread[10];
        Thread thread = null;
        long length;
        Scanner in = new Scanner( System.in );

        for( int i = 0; i < 10; i++) {
            System.out.print("Enter the duration (in ms) of task " + i + ": ");
            length = in.nextLong();
            timer = new Timer( length, i );
            thread = new Thread(timer);
            thread.start();

            boolean running = false;
            for (int j=0; j<i; j++){
                if (threads[j].isAlive()){
                    running = true;
                }
            }
            if (!running && !Timer.finished_tasks.equals("")){
                System.out.println("Finished tasks: " + Timer.finished_tasks);
                Timer.finished_tasks = "";
            }

            threads[i] = thread;
        }

        boolean finished = true;
        while (true) {
            finished = true;
            for( int i = 0; i < 10; i++ ) {
                if (threads[i].isAlive()){
                    finished = false;
                }
            }
            if (finished) break;
        }
        if (finished) System.out.println("Finished tasks: " + Timer.finished_tasks);

        in.close();
    }
}
