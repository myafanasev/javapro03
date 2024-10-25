package ru.innotech;

import java.util.LinkedList;
import java.util.List;

public class MyThreadPool {
    List<Runnable> threads = new LinkedList<>(); // очередь задач на исполнение
}
