package usercode.popii.gokartrace;

import java.util.concurrent.ThreadLocalRandom;

public class Kart implements Runnable{

    public static final int MAX_FUEL = 1000;
    public static final int MIN_FUEL_PER_LAP = 80;
    public static final int MAX_FUEL_PER_LAP = 120;
    public static final int NUMBER_OF_LAPS = 3;
    private static int kartPlacement = 1;

    private int fuel;
    private final PitStop pitstop;

    Kart(PitStop pitstop) {
        this.fuel = MAX_FUEL;
        this.pitstop = pitstop;
    }

    public int getFuel() {
        return this.fuel;
    }

    public void resetFuel() {
        this.fuel = MAX_FUEL;
    }

    public void run() {
        for (int lap = 2; lap <= NUMBER_OF_LAPS; lap++) {
            int fuelConsumption = ThreadLocalRandom.current().nextInt(MIN_FUEL_PER_LAP, MAX_FUEL_PER_LAP);

            for (int i = fuel; i > fuelConsumption; i -= 20) {
                fuel = i;
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(0, 500));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.printf("%s needs to refuel!\n", Thread.currentThread().getName());
            pitstop.refuel(this);
            System.out.printf("%s is on lap %d\n", Thread.currentThread().getName(), lap);
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(0, 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        System.out.printf(">> %s finished in place: %d!\n", Thread.currentThread().getName(), kartPlacement);
        kartPlacement++;
    }
}


