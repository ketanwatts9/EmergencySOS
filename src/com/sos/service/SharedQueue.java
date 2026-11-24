package com.sos.service;

import java.util.LinkedList;

/**
 * Producer-consumer buffer (generic class). The SOS code PUTS jobs, the dispatcher thread TAKES them.
 * wait() / notifyAll() make the two threads coordinate.
 */
public class SharedQueue<T> {
    private final LinkedList<T> list = new LinkedList<>();
    private final int capacity;

    public SharedQueue(int capacity) { this.capacity = capacity; }

    public synchronized void put(T item) throws InterruptedException {
        while (list.size() == capacity) {   // queue full -> producer waits
            wait();
        }
        list.addLast(item);
        notifyAll();                        // wake up a waiting consumer
    }

    public synchronized T take() throws InterruptedException {
        while (list.isEmpty()) {            // queue empty -> consumer waits
            wait();
        }
        T item = list.removeFirst();
        notifyAll();                        // wake up a waiting producer
        return item;
    }
}
