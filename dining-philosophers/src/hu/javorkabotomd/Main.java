package hu.javorkabotomd;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * @author Botond Javorka
 * 2020.06.18
 * */

public class Main {

    private static int n;
    private static int countFinished;

    public static void main(String[] args) {
        if(!setN(args))
            return;
        Logging.init();
        Table.init(n);
        creationOfPhilosophers(n);
    }

    /**
     * Parancssori parameter lekezelese. Ha nem kap parametert 5-re allitja n-t, ha kap akkor a parameterben megadott
     * szamra, es true ertekkel ter vissza. Ha a parameter nem szam false ertekkel ter vissza.
     * @param args : Parancssori parameter.
     * */
    private static boolean setN(String[] args) {
        if(args.length > 0) {
            try {
                n = Integer.parseInt(args[0]);
            }
            catch(NumberFormatException e) {
                System.out.println("Rossz input!");
                return false;
            }
        } else
            n = 5;
        return true;
    }

    /**
     * N darab filozofus letrehozasa.
     * @param n : Filozofusok darabszama
     * */
    private static void creationOfPhilosophers(int n) {
        ExecutorService executorService = Executors.newFixedThreadPool(n);
        for (int i = 0; i < n; i++) {
            Philosopher philosopher = new Philosopher(i, n);
            executorService.submit(philosopher);
        }
        //executorService.shutdown();
    }

    /**
     * Megvizsgalja, hany filozofus fejezte be az etkezest ha az osszes akkor meghivja a logPhilosopherEatenLogList() fuggvenyt
     * */
    public static void mealsOfPhilosophersLog() {
        if(n == ++countFinished){
            Logging.logPhilosopherEatenList();
        }
    }
}