package TreadPoolUtil;

/**
 * @author Yuzhe You (No.1159774)
 * Queue Interface.
 */

public interface RunnableQueue {

    // When a new task comes in, it will be offered to the queue first
    void offer(Runnable runnable);

    // The worker thread obtains runnable through the take method
    Runnable take();

    // Gets the number of tasks in the task queue
    int size();
}
