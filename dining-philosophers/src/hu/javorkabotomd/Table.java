package hu.javorkabotomd;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Asztal reprezentacioja
 * */

public class Table {
    public static ReentrantLock extraChair;
    public static boolean[] isRightPrefered;
    public static boolean[] chairs;
    public static ReentrantLock[] forks;

    public static void init(int n) {
        extraChair = new ReentrantLock();
        isRightPrefered = new boolean[n];
        chairs = new boolean[n];
        forks = new ReentrantLock[n];
        for(int i = 0; i < n; i++) {
            forks[i] = new ReentrantLock();
        }
    }
}