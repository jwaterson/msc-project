package usercode;

public class BasicMultithreadedPrinting {
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 4; i++) {
            Thread th = new Thread(() -> {
                System.out.println("Hello");
                System.out.println("Hello again");
                System.out.println("Hello once more");
            });
            th.start();
        }
    }
}