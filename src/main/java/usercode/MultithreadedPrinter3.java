package usercode;

import instrumentation.QueueMapMediator;

public class MultithreadedPrinter3 {

    public static void main(String[] args) {

        for (int i = 0; i < 4; i++) {
            Thread th = new Thread(new PrintIt(), String.valueOf(i));
            th.start();
        }
        QueueMapMediator.printOutput(); // for ease of testing
    }
}

class PrintIt implements Runnable {

    @Override
    public void run() {
        System.out.println("Hello");
        System.out.println("Hello again");
        System.out.println("Hello one last time");
    }
}
