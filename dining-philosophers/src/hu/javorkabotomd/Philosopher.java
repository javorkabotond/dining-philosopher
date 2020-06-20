package hu.javorkabotomd;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class Philosopher implements Runnable {
    private int id; // filozofus azonositoja
    private int currentChair; // jelenlegi szek
    private int eatenFood; // megevett ennivalo
    private int neededFood; // szukseges ennivalo
    Random rng = new Random();

    public Philosopher(int id, int neededFood) {
        this.id = id;
        this.eatenFood = 0;
        this.neededFood = neededFood;
    }

    @Override
    public void run() {
        // Leul az extra szekre
        sittingToExtraChair();
        // Eldonti melyik kezet preferalja
        sleepForRandom(id);
        decideOnPreferredHand();

        while (eatenFood < neededFood) {
            if (currentChair >= 0) {
                // Az asztalnal ul
                int randomChoice = rng.nextInt(3);
                philosopherRandomChoice(randomChoice);
            } else {
                // Az extraszeken ul
                sleepForRandom(id);
                sitIntoAChairAtTable();
            }
        }
        // Filozofusok etkezeseinek kiiratasa
        Main.mealsOfPhilosophersLog();
        Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". filozofus befejezte az evest.");
    }

    /**
     * A parameterben megkapott ertek alapjan valaszt a lehetosegek kozul
     * @param randomChoice: 1-3 kozotti szam
     * */
    private void philosopherRandomChoice(int randomChoice) {
        switch (randomChoice) {
            case 0:
                // Eszik
                sleepForRandom(id);
                eating();
                break;
            case 1:
                // Atul a extra szekre
                sleepForRandom(id);
                sitIntoTheExtraChair();
                break;
            case 2:
                // Atul egy masik szekre az asztalnal.
                sleepForRandom(id);
                sitIntoADifferentChairAtTheTable();
                break;
        }
    }

    /**
     * Szinkronizalja az asztal melletti szekeket es vegiq iteral rajtuk amig a feltetel nem teljesul.
     * */
    private void sitIntoAChairAtTable() {
        synchronized (Table.chairs) {
            for (int i = 0; i < Table.chairs.length; i++) {
                // At ul az asztalhoz
                if (checkSittingOnTheChair(i)) return;
            }
        }
    }

    /**
     * Megvizsgalja, hogy a parameteben kapot i pozicioban levo szeken ulnek-e. Ha nem ulnek rajta akkor a filozofus atul
     * es feloldja az extra szeket, beallitja a jelenlegi szeket, majd igaz ertekkel ter vissza. Ha ulnek rajta hamis ertekkel ter vissza.
     * @param i: szek pozicioja az asztal mellett
     * */
    private boolean checkSittingOnTheChair(int i) {
        if (!Table.chairs[i]) {
            Table.chairs[i] = true;
            Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". filozofus atul az asztalhoz a(z) " + (i + 1) + ". szekre");
            Table.extraChair.unlock();
            currentChair = i;
            return true;
        }
        return false;
    }

    /**
     * Szinkronizalja az asztal melletti szekeket es vegiq iteral a szekeken kiveve a filozofus jelenlegi szeket nem veszi
     * figyelembe.
     * */
    private void sitIntoADifferentChairAtTheTable() {
        synchronized (Table.chairs) {
            for (int i = 0; i < Table.chairs.length; i++) {
                // Atul egy masik szekre
                if(i != currentChair){
                    if(!Table.chairs[i]) {
                        Table.chairs[i] = true;
                        Table.chairs[currentChair] = false;
                        currentChair = i;
                        Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". filozofus atult a szekre: " + (currentChair + 1) + ".");
                        return;
                    }
                    Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". filozofus nem tudt atulni a(z) " + (i + 1) + ". a szekre.");
                }
            }

        }
    }

    /**
     * Megvizsgalja, hogy a extra szek foglalt vagy nem. Ha nem akkor lefoglalja. Szinkronizalja az asztal meleltti szekeket es
     * beaalitja a jelenlegi szek poziciojat. Majd meghivja a decideOnPreferredHand() fuggvenyt.
     * */
    private void sitIntoTheExtraChair() {
        if (!Table.extraChair.isLocked()) {
            Table.extraChair.lock();
            synchronized (Table.chairs) {
                Table.chairs[currentChair] = false;
                Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". filozofus kiult az extraszekre");
            }
            currentChair = -1;
            // Eldonti melyik kezet preferalja
            sleepForRandom(id);
            decideOnPreferredHand();
        } else {
            Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". nem tudott leulni az extra szekre.");
        }

    }

    /**
     * Megvizsgalja, hogy jobb vagy bal oldali kezet preferalja a filozofus
     * */
    private void eating() {
        if (Table.isRightPrefered[id]) {
            preferedRightHand();
        } else {
            preferedLeftHand();
        }
        Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". filozofus a(z) " + (currentChair + 1) + ". szeken evett: " + eatenFood);
        Logging.fillPhilosopherEatenLogList("A(z) " + (id + 1) + ". filozofus a(z) " + (currentChair + 1) + ". szeken evett: " + eatenFood);
    }

    /**
     * A filozofus etkezese bal oldali kezzel, megvizsgalja, hogy hol foglal helyet a filozofus es az alapjan foglalja le
     * a villakat.
     * */
    private void preferedLeftHand() {
        if (currentChair > 0) {
            Table.forks[currentChair - 1].lock();
            Table.forks[currentChair].lock();
            eatenFood++;
            Table.forks[currentChair - 1].unlock();
            Table.forks[currentChair].unlock();
        } else {
            Table.forks[Table.forks.length - 1].lock();
            Table.forks[currentChair].lock();
            eatenFood++;
            Table.forks[Table.forks.length - 1].unlock();
            Table.forks[currentChair].unlock();
        }
    }

    /**
     * A filozofus etkezese jobb oldali kezzel, megvizsgalja, hogy hol foglal helyet a filozofus es az alapjan foglalja le
     * a villakat.
     * */
    private void preferedRightHand() {
        Table.forks[currentChair].lock();
        if (currentChair > 0) {
            Table.forks[currentChair - 1].lock();
            eatenFood++;
            Table.forks[currentChair - 1].unlock();
        } else {
            Table.forks[Table.forks.length - 1].lock();
            eatenFood++;
            Table.forks[Table.forks.length - 1].unlock();
        }
        Table.forks[currentChair].unlock();
    }

    /**
     * Extra szek lefoglalasa
     * */
    private void sittingToExtraChair() {
        Table.extraChair.lock();
        currentChair = -1;
        Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". filozofus rault az extra szekre.");
    }

    /**
     * A filozofus eldonti melyik kezet preferalja
     * */
    private void decideOnPreferredHand() {
        synchronized (Table.isRightPrefered) {
            boolean deadlock;
            boolean firstPreference;
            if (id == 0) {
                firstPreference = Table.isRightPrefered[1];
            } else {
                firstPreference = Table.isRightPrefered[0];
            }
            // Megvizsgalja, hogy a tomb minden eleme egyforma-e
            deadlock = isDeadlock(firstPreference);
            // Eldonti melyik kezet preferalja
            decideHand(deadlock, firstPreference);
            Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". filozofus valasztott kezet.");
        }
    }

    /**
     * Megvizsgalja, hogy mindegyik filozofus egyforma kezet valasztott-e
     * @param firstPreference: az elso nem sajat preferalt kez
     * */
    private boolean isDeadlock(boolean firstPreference) {
        for (int i = 0; i < Table.isRightPrefered.length; i++) {
            if (i != id) {
                if (Table.isRightPrefered[i] != firstPreference) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Ha a deadlock true akkor akkor masik erteket valasztja, ha hamis akkor random valszat egy erteket.
     * @param deadlock: lehet-e potenciali holtpont
     * @param firstPreference: az elso nem sajat preferalt kez
     * */
    private void decideHand(boolean deadlock, boolean firstPreference) {
        if (deadlock) {
            Table.isRightPrefered[id] = !firstPreference;
        } else {
            Table.isRightPrefered[id] = rng.nextBoolean();
        }
    }

    /**
     * A filozofus varakozik cselekves elott.
     * */
    private void sleepForRandom(int id) {
        int seconds = getRandomNumber(500,2000);
        try {
            TimeUnit.MILLISECONDS.sleep(seconds);
            Logging.logToFile(Level.INFO,"A(z) " + (id + 1) + ". varakozot: " + seconds + " milliszekundumot.");
        } catch (InterruptedException e) {
            System.out.println("Hiba a filozofusok varakozasanal");
        }
    }

    /**
     * Random szamot ad vissza megadott ertekek kozott
     * @param min: minimum ertrek
     * @param max: maximum ertek
     * */
    private int getRandomNumber(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }
}