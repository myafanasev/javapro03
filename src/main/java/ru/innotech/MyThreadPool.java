package ru.innotech;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPool {
    List<Runnable> tasks = new LinkedList<>(); // очередь задач на исполнение
    List<MyThread> threadPoll;   // очередь потоков
    final Lock lockThread;  // для потоков
    final Condition conditionThread;
    boolean stopThreads;
    final CountDownLatch countDownLatch; // для awaitTermination


    public MyThreadPool(int capacity) { // конструктор с ёмкостью
        if (capacity < 1) throw new IllegalArgumentException("Размер пула потоков не может быть меньше одного");
        lockThread = new ReentrantLock();
        conditionThread = lockThread.newCondition();
        stopThreads = false;
        countDownLatch = new CountDownLatch(capacity);

        threadPoll = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) { // создаём потоки
            MyThread myThread = new MyThread(i);
            threadPoll.add(myThread);
            myThread.start();
        }
    }
    class MyThread extends Thread {
        int number; // номер потока для визуализации

        public MyThread(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            while(true) {
                System.out.println("Поток " + number);
                lockThread.lock(); // не используем lockInterruptibly, иначе есть риск, что после вызова interrupt() мы не проверим, есть ли задачи в очереди
                try {
                    while (tasks.isEmpty()) {
                        System.out.println("Поток " + number + ": переходим в ожидание");
                        conditionThread.await();  // если уже вызван interrupt(), то здесь мы и остановим поток
                    }
                } catch (InterruptedException e) {
                    System.out.println("Остановка потока " + number);
                    countDownLatch.countDown(); // для awaitTermination
                    return;
                }
                finally {
                    lockThread.unlock();
                }
                Runnable runnable = getRunnable();
                if (runnable != null) {
                    System.out.println("Поток " + number + ": исполняем задачу");
                    runnable.run();   // запускаем задачу на исполнение
                }
            }
        }
    }
    public void execute(Runnable command) {    // добавление потока в очередь и запуск на исполнение, если в пуле есть место
        if(stopThreads) throw new IllegalStateException("Пул потоков больше не принимает задачи");
        if (command == null) throw new NullPointerException();

        lockThread.lock();
        tasks.add(command); // добавляем задачу в очередь на исполнение
        System.out.println("Задание добавлено");
        conditionThread.signal(); // пробуждаем один из ждущих потоков, чтобы он мог забрать задачу на исполнение
        lockThread.unlock();
    }

    private synchronized Runnable getRunnable() {    // получение задачи из очереди
            if (tasks.isEmpty()) return null; // на случай, если другие потоки успели забрать задачи
            Runnable runnable = tasks.get(0);
            tasks.remove(0);
            return runnable;
    }

    public void shutdown() {
        System.out.println("Вызван метод shutdown");
        stopThreads = true;
        for(MyThread t : threadPoll)
            t.interrupt();
    }

    public void awaitTermination() {
        System.out.println("Вызван метод awaitTermination");
        shutdown();
        try {
            countDownLatch.await(); // ждём пока отработают все потоки
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
