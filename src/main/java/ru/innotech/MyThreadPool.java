package ru.innotech;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPool {
    List<Runnable> tasks = new LinkedList<>(); // очередь потоков на исполнение
    Queue<Thread> threadPoll;   // очередь потоков
    Lock monitor = new ReentrantLock();

    public MyThreadPool(int capacity) { // конструктор с ёмкостью
        if (capacity < 1 ) throw new IllegalArgumentException("Размер пула потоков не может быть меньше одного");
        threadPoll = new ArrayBlockingQueue(capacity);
        //monitor.lock(); // сразу блокируем монитор
        //monitor.notify();
        for(int i = 0; i < capacity; i++) {
            int w = i;
            threadPoll.add(new Thread(()->
            {
                while(true) {
                    System.out.println("Поток " + w);
                    monitor.lock();
                    try {
                        if(!tasks.isEmpty()) {
                            tasks.get(0).run(); // выполняем метод из очереди
                        }
                        monitor.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }));}
        for (Thread r : threadPoll) {
            r.start();
        }

    }

    public void execute(Runnable command) {    // добавление потока в очередь и запуск на исполнение, если в пуле есть место
        if (command == null) throw new NullPointerException();
        tasks.add(command);
        monitor.lock();
        monitor.notify();
    }

}
