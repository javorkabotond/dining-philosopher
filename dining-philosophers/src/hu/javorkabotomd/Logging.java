package hu.javorkabotomd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {
    private static long startTime; // program inditasa
    private static ArrayList<String> philosopherEatenLog = new ArrayList<>();
    private static FileHandler fileHandler; //
    private static Logger globalLogger; //

    /**
     * Inicializalja a Logging osztalyt
     * */
    public static void init() {
        startTime = System.currentTimeMillis();
        globalLogger = Logger.getGlobal();
        globalLogger.setUseParentHandlers(false);
        globalLogger.setLevel(Level.ALL);
        try {
            fileHandler = new FileHandler("log.txt",true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            globalLogger.addHandler(fileHandler);
        }catch (IOException e) {
            System.out.println("Hiba a fajl letrehozasaban");
        }
    }

    /**
     * Egy log fajlba kiirja a kapott szoveget
     * @param level: kiirt log szintje
     * @param message: kiirt szoveg
     * */
    public static void logToFile(Level level, String message){
        globalLogger.log(level, message);
    }

    /**
     * Egy listaba gyujti ossze a kapott szoveget
     * @param message: kiirt szoveg
     * */
    public static void fillPhilosopherEatenLogList(String message){
        // A program indulasatol szamitott idot szamolja ki milliszekundumban.
        long deltaTime = System.currentTimeMillis() - startTime;
        philosopherEatenLog.add(Level.INFO +": "+deltaTime + " milliszekundum " + message);
    }

    /**
     * A listaban osszegyujtot szoveget irja ki
     * */
    public static void logPhilosopherEatenList(){
        for (String s : philosopherEatenLog) {
            System.out.println(s);
        }
    }
}