package TreadPoolUtil;

/**
 * @author Yuzhe You (No.1159774)
 * Deny policy.
 */

@FunctionalInterface
public interface DenyPolicy {
    void reject(Runnable runnable, ThreadPool threadPool);

    // Rejection strategy of abandoning task directly
    class DiscardDenPolicy implements DenyPolicy{
        @Override
        public void reject(Runnable runnable, ThreadPool threadPool) {
            //do nothing
        }
    }

    // The rejection policy throws an exception to the task committer
    class AbortDenyPolicy implements DenyPolicy{
        @Override
        public void reject(Runnable runnable, ThreadPool threadPool) {
            throw new RunnableDenyException("The runnable"+runnable+"will be aborted");
        }
    }

    // This rejection policy causes the task to execute the task in the thread where the submitter is located
    class RunnerDentPolicy implements  DenyPolicy{
        @Override
        public void reject(Runnable runnable, ThreadPool threadPool) {
            if(!threadPool.isShutdown()){
                runnable.run();
            }
        }
    }
}