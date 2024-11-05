package ru.innotech;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPool {
    List<Runnable> tasks = new LinkedList<>(); // очередь задач на исполнение
    List<Thread> threadPoll;   // очередь потоков
    final public Lock monitorGetRunnable = new ReentrantLock();
    final Lock lockThread;
    final Condition condition;
    boolean stopThreads;


    public MyThreadPool(int capacity) { // конструктор с ёмкостью
        if (capacity < 1 ) throw new IllegalArgumentException("Размер пула потоков не может быть меньше одного");
        lockThread = new ReentrantLock();
        condition = lockThread.newCondition();
        stopThreads = false;

        threadPoll = new ArrayList<>(capacity);
        for(int i = 0; i < capacity; i++) { // создаём потоки
            int w = i;
            threadPoll.add(new Thread(()->
            {
                while(true) {
                    try {
                        System.out.println("Поток " + w);
                        lockThread.lockInterruptibly();
                        try {
                            while (tasks.isEmpty()) {
                                System.out.println("Поток " + w + ": переходим в ожидание");
                                condition.await();
                            }
                        } finally {
                            lockThread.unlock();
                        }
                        Runnable runnable = getRunnable();
                        if (runnable != null) {
                            System.out.println("Поток " + w + ": исполняем задачу");
                            runnable.run();   // запускаем задачу на исполнение
                        }
                    } catch (InterruptedException e) {
                        System.out.println("Остановка потока " + w);
                        return;
                    }
                }
            }));}
        for (Thread r : threadPoll) {   // запускаем потоки
            r.start();
        }
    }

    public void execute(Runnable command) {    // добавление потока в очередь и запуск на исполнение, если в пуле есть место
        if(stopThreads) throw new IllegalStateException("Пул потоков больше не принимает задачи");
        if (command == null) throw new NullPointerException();

        System.out.println("Пытаемся добавить задание");
        lockThread.lock();
        tasks.add(command); // добавляем задачу в очередь на исполнение
        System.out.println("Задание добавлено");
        condition.signal(); // пробуждаем один из ждущих потоков, чтобы он мог забрать задачу на исполнение
        lockThread.unlock();
    }

    private synchronized Runnable getRunnable() {    // получение задачи из очереди
            if (tasks.isEmpty()) return null; // на случай, если другие потоки успели забрать задачи
            Runnable runnable = tasks.get(0);
            tasks.remove(0);
            return runnable;
    }

    public void shutdown() {
        stopThreads = true;
        for(Thread t : threadPoll)
            t.interrupt();
    }

}
