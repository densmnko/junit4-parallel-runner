package org.densmko;

import org.junit.runners.model.RunnerScheduler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NaiveScheduler implements RunnerScheduler {

    private final ExecutorService[] pools;
    private final AtomicInteger counter2 = new AtomicInteger(0);

    private final Phaser counter = new Phaser();
    private final NaiveThreadFactory threadFactory;

    public NaiveScheduler(int length) {
        pools  = new ExecutorService[length];
        threadFactory = new NaiveThreadFactory();
        for ( int i = 0; i < length; i++) {
            pools[i] = Executors.newSingleThreadExecutor(threadFactory);
        }
    }

    @Override
    public void schedule(final Runnable runner) {
            int lane = counter2.incrementAndGet() % 2; // todo: resolve lane
            counter.register();
            pools[lane].submit(() -> {
                runner.run();
                counter.arrive();
            });
    }

    @Override
    public void finished() {
        counter.awaitAdvance(0);
    }

    private static class NaiveThreadFactory implements ThreadFactory {
        final ThreadGroup group = new ThreadGroup("parallel-runner");
        AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(group,r, String.format("runner-%d",counter.incrementAndGet()));
        }
    }
}