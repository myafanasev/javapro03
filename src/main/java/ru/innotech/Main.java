package ru.innotech;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        MyThreadPool t = new MyThreadPool(4);

        for(int i = 0; i < 10; i++) {
            int e = i;
            t.execute(() -> {System.out.println("Задание " + e + " исполнено");
            });
        }

        Thread.sleep(2000);

        for(int i = 10; i < 20; i++) {
            int e = i;
            t.execute(() -> System.out.println("Задание " + e + " исполнено"));
        }

        t.shutdown();
        //t.awaitTermination();

        System.out.println("Конец главного потока");
    }
}
