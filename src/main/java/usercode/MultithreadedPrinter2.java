package usercode;

import instrumentation.QueueMapMediator;

public class MultithreadedPrinter2 {

    public static void main(String[] args) {

        for (int i = 0; i < 4; i++) {
            Thread th = new Thread(() -> {
                System.out.println("Hello");
                System.out.println("Hello again");
                System.out.println("Hello one last time");
            }, String.valueOf(i));
            th.start();
        }
        QueueMapMediator.printOutput(); // for ease of testing
    }

}
