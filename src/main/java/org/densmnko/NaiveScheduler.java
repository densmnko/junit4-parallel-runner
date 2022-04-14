package org.densmnko;

import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NaiveScheduler implements RunnerScheduler {

    private final ExecutorService[] pools;
    private final ParallelSuiteRunner parallelSuiteRunner;
    private final AtomicInteger counter2 = new AtomicInteger(0);

    private final Phaser counter = new Phaser();
    private final NaiveThreadFactory threadFactory;


    public NaiveScheduler(int length, ParallelSuiteRunner parallelSuiteRunner) {
        pools = new ExecutorService[length];
        this.parallelSuiteRunner = parallelSuiteRunner;
        threadFactory = new NaiveThreadFactory();
        for (int i = 0; i < length; i++) {
            pools[i] = Executors.newSingleThreadExecutor(threadFactory);
        }
    }

    @Override
    public void schedule(final Runnable runner) {
        if (runner instanceof ParallelSuiteRunner.ParallelRunnable) {
            int lane = parallelSuiteRunner.laneOf(((ParallelSuiteRunner.ParallelRunnable) runner).getRunner());
            counter.register();
            pools[lane].submit(() -> {
                runner.run();
                counter.arrive();
            });
        } else {
            throw new IllegalArgumentException("unknown runner " + runner);
        }
    }

    @Override
    public void finished() {
        counter.awaitAdvance(0);
    }

    private class NaiveThreadFactory implements ThreadFactory {
        final ThreadGroup group = new ThreadGroup("parallel-runner");
        final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            int i = counter.getAndIncrement();
            Thread thread = new Thread(group, r, String.format("runner-%d", i));
            thread.setContextClassLoader(parallelSuiteRunner.classLoaders.get(i));
            return thread;
        }

    }

}