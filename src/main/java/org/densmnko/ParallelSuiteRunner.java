package org.densmnko;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;

import java.io.PrintStream;
import java.lang.annotation.*;
import java.util.*;


/**
 * todo
 */
public class ParallelSuiteRunner extends ParentRunner<Runner> {

    private final List<Runner> runners;
    final List<ParallelRunnerClassLoader> classLoaders;
    private final Map<Runner, Integer> runnersLanes;

    private final PrintStream systemOut;
    private final RunnerOutputStream runnerStream;
    private final NaiveScheduler scheduler;

    /**
     * The <code>Suites</code> annotation specifies the list of <code>SuiteClasses</code> to be run in parallel when a class
     * annotated with <code>@RunWith(ParallelSuite.class)</code> is run.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface Suites {
        Suite.SuiteClasses[] value();
        String[] isolate();
    }

    /**
     * Called reflectively on classes annotated with <code>@RunWith(ParallelSuite.class)</code>
     *
     * @param klass   the root class
     * @param builder builds runners for classes in the suite
     */
    public ParallelSuiteRunner(Class<?> klass, RunnerBuilder builder) throws InitializationError, ClassNotFoundException {
        super(klass);
        final Suites annotation = klass.getAnnotation(Suites.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a Suites annotation", klass.getName()));
        }
        Suite.SuiteClasses[] suites = annotation.value();
        final Map<Runner, Integer> runnersLanes = new HashMap<>();
        final List<Runner> runners = new ArrayList<>();
        classLoaders = new ArrayList<>();
        ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
        for (int i = 0; i < suites.length; i++) {
            final int lane = i;
            ParallelRunnerClassLoader classLoader = new ParallelRunnerClassLoader(lane, parentClassLoader, annotation.isolate() );
            classLoaders.add(classLoader);
            List<Class<?>> classes = new ArrayList<>();
            for ( Class<?> aClass : suites[lane].value() ) {
                Class<?> aClassForLane = Class.forName(aClass.getName(), true, classLoader);
                classes.add(aClassForLane);
            }
            List<Runner> laneRunners = builder.runners(klass, classes);
            runners.addAll(laneRunners);
            laneRunners.forEach(r -> runnersLanes.put(r, lane));
        }
        this.runnersLanes = Collections.unmodifiableMap(runnersLanes);
        this.runners = Collections.unmodifiableList(runners);
        scheduler = new NaiveScheduler(suites.length, this);
        setScheduler(scheduler);
        systemOut = System.out;
        System.out.flush();
        runnerStream = new RunnerOutputStream();
        System.setOut(new PrintStream(runnerStream));
    }



    protected List<Runner> getChildren() {
        return runners;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    static class RunNotifierRecorder extends RunNotifier {

        private final RunnerOutputStream runnerStream;
        private final Thread currentThread;
        private final PrintStream systemOut;

        RunNotifierRecorder(RunnerOutputStream runnerStream, Thread currentThread, PrintStream systemOut) {
            this.runnerStream = runnerStream;
            this.currentThread = currentThread;
            this.systemOut = systemOut;
        }

        enum Method {
            addFirstListener, addListener, removeListener, fireTestRunStarted, fireTestRunFinished, fireTestSuiteStarted, fireTestSuiteFinished, fireTestStarted, fireTestFailure, fireTestAssumptionFailed, fireTestIgnored, fireTestFinished, pleaseStop
        }

        static class Event {
            final Method method;
            final Object parameter;
            final String output;
            final String generalOut;

            Event(Method method, Object parameter, String output, String generalOut) {
                this.method = method;
                this.parameter = parameter;
                this.output = output;
                this.generalOut = generalOut;
            }
        }

        final List<Event> events = new ArrayList<>();


        @Override
        public void addFirstListener(RunListener listener) {
            throw new UnsupportedOperationException("notifier.addFirstListener(listener)");
        }

        @Override
        public void addListener(RunListener listener) {
            throw new UnsupportedOperationException("notifier.addListener(listener)");
        }

        @Override
        public void removeListener(RunListener listener) {
            System.out.println("notifier.removeListener(listener): " + listener);
            throw new UnsupportedOperationException("notifier.removeListener(listener)");
        }

        @Override
        public void fireTestRunStarted(Description description) {
            System.out.println("notifier.fireTestRunStarted(description): " + description);
            record(Method.fireTestRunStarted, description);
        }

        @Override
        public void fireTestRunFinished(Result result) {
            System.out.println("notifier.fireTestRunFinished(result): " + result);
            record(Method.fireTestRunFinished, result);
        }

        @Override
        public void fireTestSuiteStarted(Description description) {
            System.out.println("notifier.fireTestSuiteStarted(description): " + description);
            record(Method.fireTestSuiteStarted, description);
        }

        @Override
        public void fireTestSuiteFinished(Description description) {
            System.out.println("notifier.fireTestSuiteFinished(description): " + description);
            record(Method.fireTestSuiteFinished, description);
        }

        @Override
        public void fireTestStarted(Description description) throws StoppedByUserException {
            System.out.println("notifier.fireTestStarted(description): " + description);
            record(Method.fireTestStarted, description);
        }

        @Override
        public void fireTestFailure(Failure failure) {
            System.out.println("notifier.fireTestFailure(failure): " + failure);
            record(Method.fireTestFailure, failure);
        }

        @Override
        public void fireTestAssumptionFailed(Failure failure) {
            System.out.println("notifier.fireTestAssumptionFailed(failure): " + failure);
            record(Method.fireTestAssumptionFailed, failure);
        }

        @Override
        public void fireTestIgnored(Description description) {
            System.out.println("notifier.fireTestIgnored(description): " + description);
            record(Method.fireTestIgnored, description);
        }

        @Override
        public void fireTestFinished(Description description) {
            System.out.println("notifier.fireTestFinished(description): " + description);
            record(Method.fireTestFinished, description);
        }

        @Override
        public void pleaseStop() {
            System.out.println("notifier.pleaseStop()");
            record(Method.pleaseStop, null);
        }

        private void record(Method method, Object parameter) {
            events.add(new Event(method, parameter, runnerStream.getAndReset(currentThread), runnerStream.getAndResetGeneral()));

        }

        protected void replay(RunNotifier notifier) {
            events.forEach(e -> replayEvent(notifier, e));
        }

        private void replayEvent(RunNotifier notifier, Event event) {
            writeSystemOut(event.generalOut, systemOut);
            writeSystemOut(event.output, systemOut);
            switch (event.method) {
                case fireTestRunStarted:
                    notifier.fireTestRunStarted((Description) event.parameter);
                    break;
                case fireTestRunFinished:
                    notifier.fireTestRunFinished((Result) event.parameter);
                    break;
                case fireTestSuiteStarted:
                    notifier.fireTestSuiteStarted((Description) event.parameter);
                    break;
                case fireTestSuiteFinished:
                    notifier.fireTestSuiteFinished((Description) event.parameter);
                    break;
                case fireTestStarted:
                    notifier.fireTestStarted((Description) event.parameter);
                    break;
                case fireTestFailure:
                    notifier.fireTestFailure((Failure) event.parameter);
                    break;
                case fireTestAssumptionFailed:
                    notifier.fireTestAssumptionFailed((Failure) event.parameter);
                    break;
                case fireTestIgnored:
                    notifier.fireTestIgnored((Description) event.parameter);
                    break;
                case fireTestFinished:
                    notifier.fireTestFinished((Description) event.parameter);
                    break;
                case pleaseStop:
                    notifier.pleaseStop();
                    break;
                default:
                    throw new IllegalStateException("unhandled method " + event.method);
            }
        }
    }

    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() {
                runChildren(notifier);
            }
        };
    }

    private void runChildren(final RunNotifier notifier) {
        final RunnerScheduler currentScheduler = scheduler;
        try {
            for (final Runner each : getChildren()) {
                currentScheduler.schedule(new ParallelRunnable(each, notifier));
            }
        } finally {
            currentScheduler.finished();
        }
    }



    protected void runChild(Runner runner, RunNotifier notifier) {
        final Thread currentThread = Thread.currentThread();
        System.out.format("%s: runChild: %s\n", currentThread, runner.getDescription());
        final RunNotifierRecorder recorder = new RunNotifierRecorder(runnerStream, currentThread, systemOut);
        final String prefix = runnerStream.getAndReset(currentThread);
        final String prefixGen = runnerStream.getAndResetGeneral();

        RuntimeException exception = null;
        try {
            runner.run(recorder);
        } catch (RuntimeException e) {
            exception = e;
        }
        synchronized (this) {
            writeSystemOut(prefix, systemOut);
            writeSystemOut(prefixGen, systemOut);
            recorder.replay(notifier);
            final String postfix = runnerStream.getAndReset(currentThread);
            writeSystemOut(postfix, systemOut);
            final String postfixGen = runnerStream.getAndReset(currentThread);
            writeSystemOut(postfixGen, systemOut);
            if (exception != null) {
                throw exception;
            }
        }
    }


    static void writeSystemOut(String string, PrintStream systemOut) {
        if (string != null && !string.isEmpty()) {
            systemOut.append(string);
        }
    }

    public class ParallelRunnable implements Runnable {

        private final Runner runner;
        private final RunNotifier notifier;

        public ParallelRunnable(Runner each, RunNotifier notifier) {
            this.runner = each;
            this.notifier = notifier;
        }

        public Runner getRunner() {
            return runner;
        }

        public void run() {
            runChild(runner, notifier);
        }
    }


    public int laneOf(Runner runner) {
        return runnersLanes.get(runner);
    }


}
