package com.sos.service;

import com.sos.exception.AppException;
import com.sos.repository.AlertRepository;

/** Background thread (the consumer). Pretends to send each SMS, one by one. */
public class DispatcherThread extends Thread {
    private final SharedQueue<Notification> queue;
    private final AlertRepository alertRepository;

    public DispatcherThread(SharedQueue<Notification> queue, AlertRepository alertRepository) {
        super("sos-dispatcher");
        this.queue = queue;
        this.alertRepository = alertRepository;
        setDaemon(true);   // daemon thread: it will not stop the program from exiting
    }

    @Override
    public void run() {
        while (true) {
            try {
                Notification job = queue.take();              // waits here if nothing to do
                Thread.sleep(800);                            // pretend network delay
                String message = job.getContact().sendAlert(job.getAlert());
                job.getAlert().markDelivered(job.getIndex());
                alertRepository.save(job.getAlert());         // store the new status in the file
                System.out.println("[Dispatcher] " + message);
            } catch (InterruptedException e) {
                System.out.println("[Dispatcher] stopped");
                return;
            } catch (AppException e) {
                System.out.println("[Dispatcher] could not save: " + e.getMessage());
            }
        }
    }
}
