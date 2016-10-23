package com.test.test;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by Fionn on 18/10/2016.
 */
public class GameWorker extends Thread{

    private final ConcurrentLinkedQueue<Runnable> taskList;
    private boolean isStopped = false;

    public GameWorker(ConcurrentLinkedQueue<Runnable> tasks){
        this.taskList = tasks;
    }

    public synchronized void run(){
        Runnable task;
        while(true){
            try{
                task = this.taskList.poll();
                if (task != null) {
                    System.out.println("-- " + Thread.currentThread().getName() +
                            " takes task. " + this.taskList.size() + " tasks in q --");
                    System.out.println("running");
                    task.run();
                    this.taskList.add(task);
                }else{
                    System.out.println(Thread.currentThread().getName() + " waiting..");
                }
            }catch(Exception e){
                System.out.println("Error: " + e.toString());
            }
        }
    }
}
