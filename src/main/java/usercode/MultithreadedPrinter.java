package usercode;


import instrumentation.QueueMapMediator;

public class MultithreadedPrinter {
    static char[] arr = "Hello World".toCharArray();

    public static void main(String[] args) {
        for (int i = 0; i < arr.length; i++) {
            Thread thread = new Thread(() -> {
                System.out.print(arr[Integer.parseInt(Thread
                        .currentThread().getName())]);
            }, String.valueOf(i));
            thread.start();
        }
        QueueMapMediator.printOutput(); // for ease of testing
    }


}
