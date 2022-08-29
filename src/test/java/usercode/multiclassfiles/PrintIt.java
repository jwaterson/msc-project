package usercode.multiclassfiles;

class PrintIt implements Runnable {

    @Override
    public void run() {
        System.out.println("Hello");
        System.out.println("Hello again");
        System.out.println("Hello one last time");
    }
}