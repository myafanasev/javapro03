package ru.innotech;

import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
       // ExecutorService service = Executors.newFixedThreadPool(4);
//        ExecutorService service = new ThreadPoolExecutor(5, 10, 1L, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10));
//        for (int i = 0; i < 10; i++) {
//            final int w = i + 1;
//            service.execute(() -> {
//                System.out.println(w + " - begin");
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e ) {
//                    e.printStackTrace();
//                }
//                System.out.println(w + " - end");
//            });
//        }
        //service.shutdown();
        //service.awaitTermination(); // ждать с таймаутом
        //Runnable t = new Thread();


    MyThreadPool t = new MyThreadPool(1);
    t.execute(()-> System.out.println("задание 1"));
//        t.execute(()-> System.out.println("Поток1"));
//      t.execute(()-> System.out.println("Поток2"));
//        System.out.println(t.tasks);

    }
}
