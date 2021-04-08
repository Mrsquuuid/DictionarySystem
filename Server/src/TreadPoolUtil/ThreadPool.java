package TreadPoolUtil;

/**
 * @author Yuzhe You (No.1159774)
 * Thread pool interface.
 */

public interface ThreadPool {

    void execute(Runnable runnable);

    void shutdown();

    int getInitSize();

    int getMaxSize();

    int getCoreSize();

    int getQueueSize();

    int getActiveCount();

    boolean isShutdown();
}
