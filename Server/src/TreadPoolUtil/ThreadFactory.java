package TreadPoolUtil;

/**
 * @author Yuzhe You (No.1159774)
 * Thread factory.
 */

// Create a factory for threads
// @Functional interface functional interface.
// There can only be one method in the interface. The 1.8 new method allows the default implementation method
@FunctionalInterface
public interface ThreadFactory {
    Thread createThread(Runnable runnable);
}