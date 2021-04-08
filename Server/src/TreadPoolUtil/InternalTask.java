package TreadPoolUtil;

/**
 * @author Yuzhe You (No.1159774)
 * Deny policy.
 */

public class InternalTask implements Runnable {
    private final RunnableQueue runnableQueue;
    private volatile boolean running = true;

    public InternalTask(RunnableQueue runnableQueue) {
        this.runnableQueue = runnableQueue;
    }

    @Override
    public void run() {
        // If the current task is running and is not interrupted, it will continuously get runnable from the queue,
        // and then run the run method
        while (running&&!Thread.currentThread().isInterrupted()){
            try {
                Runnable task = runnableQueue.take();
                task.run();
            } catch (Exception e) {
                running = false;
                break;
            }
        }
    }
    // To stop the current task, it is mainly used in the shutdown method of the thread pool.
    public void stop(){
        this.running = false;
    }
}