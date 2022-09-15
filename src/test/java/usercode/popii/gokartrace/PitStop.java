package usercode.popii.gokartrace;

public class PitStop {

    private boolean occupied = false;

    public synchronized void refuel(Kart kart) {
        while (occupied) {
            try {
                wait();
            } catch (InterruptedException e)  {
                Thread.currentThread().interrupt();
            }
        }

        System.out.printf("%s is now refuelling...\n", Thread.currentThread().getName());

        try {
            Thread.sleep(Kart.MAX_FUEL - kart.getFuel());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        kart.resetFuel();
        occupied = false;
        notifyAll();
    }

    public static void main(String[] args) {
        int numberOfKarts = 3;
        PitStop pitStop = new PitStop();
        Thread[] karts = new Thread[numberOfKarts];

        for (int i = 0; i < numberOfKarts; i++) {
            Thread kart = new Thread(new Kart(pitStop), "Kart " + i);
            karts[i] = kart;
            kart.start();
        }

        for (Thread k : karts) {
            try {
                k.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}


