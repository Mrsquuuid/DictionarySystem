package TreadPoolUtil;

import java.util.LinkedList;

/**
 * @author Yuzhe You (No.1159774)
 * Queue of threads.
 */

public class LinkedRunnableQueue implements RunnableQueue {
    // The maximum capacity of the task queue, which is passed in during construction
    private final int limit;

    // If the task in the task queue is full, a reject policy is required
    private final DenyPolicy denyPolicy;

    // The queue where tasks are stored
    private final LinkedList<Runnable> runnableList = new LinkedList<>();

    private final ThreadPool threadPool;

    public LinkedRunnableQueue(int limit, DenyPolicy denyPolicy, ThreadPool threadPool) {
        this.limit = limit;
        this.denyPolicy = denyPolicy;
        this.threadPool = threadPool;
    }

    @Override
    public void offer(Runnable runnable) {
        synchronized (runnableList){
            if(runnableList.size()>=limit){
                // Execute the reject policy when a new task cannot be accommodated
                denyPolicy.reject(runnable,threadPool);
            }else {
                // Add the task to the end of the queue and wake up the blocked thread
                runnableList.addLast(runnable);
                runnableList.notifyAll();

            }
        }

    }

    @Override
    public Runnable take(){
        synchronized (runnableList){
            while (runnableList.isEmpty()){
                try {
                    // If there is no executable task in the task queue,
                    // the current thread will hang and enter the runnablelist associated thread
                    // Waiting to wake up in monitor waitset
                    runnableList.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Remove a task from the head of the task queue
            return runnableList.removeFirst();
        }
    }

    @Override
    public int size() {
        synchronized (runnableList){
            // Returns the number of tasks in the current task queue
            return runnableList.size();
        }
    }
}