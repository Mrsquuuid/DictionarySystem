package TreadPoolUtil;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yuzhe You (No.1159774)
 * Basic thread pool.
 */

public class BasicThreadPool extends Thread implements ThreadPool {
    // Initialize the number of threads
    private final int initSize;

    // Maximum number of threads in thread pool
    private final int maxSize;

    // Number of core threads in thread pool
    private final int coreSize;

    // Current active quantity
    private int activeCount;

    // Factory needed to create thread
    private final ThreadFactory threadFactory;

    // Task queue
    private final RunnableQueue runnableQueue;

    // Whether the thread pool has been shut down. The default value is false
    private volatile boolean isShutdown = false;

    // Worker thread queue
    private final Queue<ThreadTask> threadQueue = new ArrayDeque<>();

    // Default rejection policy
    private final static DenyPolicy DEFAULT_DENY_POLICY = new DenyPolicy.DiscardDenPolicy();

    // Default thread factory
    private final static ThreadFactory DEFAULT_THREAD_FACTORY = new DefaultThreadFactory();

    private  final long keepAliveTime;

    private final TimeUnit timeUnit;

    public BasicThreadPool(int initSize, int maxSize, int coreSize,int queueSize) {
        this(initSize,maxSize,coreSize,DEFAULT_THREAD_FACTORY,queueSize,DEFAULT_DENY_POLICY,10,TimeUnit.SECONDS);
    }

    public BasicThreadPool(int initSize, int maxSize, int coreSize, ThreadFactory threadFactory, int queueSize, DenyPolicy denyPolicy, long keepAliveTime, TimeUnit timeUnit) {
        this.initSize = initSize;
        this.maxSize = maxSize;
        this.coreSize = coreSize;
        this.threadFactory = threadFactory;
        this.runnableQueue = new LinkedRunnableQueue(queueSize,denyPolicy,this);
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.init();
    }

    private void newThread(){
        // Create a task thread and start it
        InternalTask internalTask = new InternalTask(runnableQueue);
        Thread thread = this.threadFactory.createThread(internalTask);
        ThreadTask threadTask = new ThreadTask(thread,internalTask);
        threadQueue.offer(threadTask);
        // Current active thread + 1
        this.activeCount++;
        thread.start();

    }
    private void removeThread(){
        // Remove a thread from the thread pool
        ThreadTask threadTask = threadQueue.remove();
        threadTask.internalTask.stop();
        // Current active thread - 1
        this.activeCount--;
    }

    // Threadtask is just a combination of interaltask and thread
    private static class ThreadTask{
        Thread thread;
        InternalTask internalTask;

        public ThreadTask(Thread thread, InternalTask internalTask) {
            this.thread = thread;
            this.internalTask = internalTask;
        }
    }
    private static class DefaultThreadFactory implements ThreadFactory{
        private static final AtomicInteger GROUP_COUNTER = new AtomicInteger(1);
        private static final ThreadGroup group = new ThreadGroup("MyThreadPool--"+GROUP_COUNTER.getAndIncrement());
        private static final AtomicInteger COUNTER = new AtomicInteger(0);
        @Override
        public Thread createThread(Runnable runnable) {
            return new Thread(group,runnable,"thread-pool-"+COUNTER.getAndIncrement());
        }
    }

    @Override
    public void run() {
        // The run method is mainly used to maintain the number of threads, expand capacity, recycle and so on
        while (!isShutdown&&isInterrupted()){
            try {
                timeUnit.sleep(keepAliveTime);
            } catch (InterruptedException e) {
                isShutdown = true;
                break;
            }
            synchronized (this){
                if(isShutdown)
                    break;
                // If there are tasks in the current queue that have not been processed and activecount < coresize continues to expand
                if(runnableQueue.size()>0&&activeCount<coreSize){
                    for(int i = initSize;i<coreSize;i++){
                        newThread();
                    }
                    // The purpose of count is not to expand the thread to maxsize directly
                    continue;
                }
                // If there are tasks in the current queue that have not been processed,
                // and activcount < maxsize, the capacity will continue to expand
                if(runnableQueue.size()>0&&activeCount<maxSize){
                    for(int i =coreSize;i<maxSize;i++)
                        newThread();
                }
                // If there is no task in the queue, it needs to be recycled and recycled to coresize
                if(runnableQueue.size()==0&&activeCount>coreSize){
                    for (int i=coreSize;i<activeCount;i++)
                        removeThread();
                }
            }
        }
    }
    // When initializing, create initsize threads first
    private void init(){
        start();
        for (int i=0;i<initSize;i++)
            newThread();
    }
    private void isShudownSystemOut(){
        if(this.isShutdown){
            throw new IllegalStateException("The thread pool is destroyed");
        }
    }
    @Override
    public void execute(Runnable runnable) {
        isShudownSystemOut();
        // The submitted task is inserted into the task queue
        this.runnableQueue.offer(runnable);
    }

    @Override
    public void shutdown() {
        synchronized (this){
            if(isShutdown) return;
            isShutdown = true;
            threadQueue.forEach(threadTask -> {
                threadTask.internalTask.stop();
                threadTask.thread.interrupt();
            });
            this.interrupt();
        }
    }

    @Override
    public int getInitSize() {
        isShudownSystemOut();
        return this.initSize;
    }

    @Override
    public int getMaxSize() {
        isShudownSystemOut();
        return this.maxSize;
    }

    @Override
    public int getCoreSize() {
        isShudownSystemOut();
        return this.coreSize;
    }

    @Override
    public int getQueueSize() {
        isShudownSystemOut();
        return runnableQueue.size();
    }

    @Override
    public int getActiveCount() {
        synchronized (this) {
            return this.activeCount;
        }
    }

    @Override
    public boolean isShutdown() {
        return this.isShutdown;
    }
}