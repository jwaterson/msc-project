package usercode.multiclassfiles;

public class MultithreadedPrinter4 {

    public static void main(String[] args) {

        for (int i = 0; i < 4; i++) {
            Thread th = new Thread(new PrintIt(), String.valueOf(i));
            th.start();
        }
    }
}