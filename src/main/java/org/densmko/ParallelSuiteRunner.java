package org.densmko;

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

import java.lang.annotation.*;
import java.util.*;


/**
 * todo
 */
public class ParallelSuiteRunner extends ParentRunner<Runner> {

    private final Map<Runner, Integer> runners;


    /**
     * The <code>Suites</code> annotation specifies the list of <code>SuiteClasses</code> to be run in parallel when a class
     * annotated with <code>@RunWith(ParallelSuite.class)</code> is run.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface Suites {
        Suite.SuiteClasses[] value();
    }


    /**
     * Called reflectively on classes annotated with <code>@RunWith(ParallelSuite.class)</code>
     *
     * @param klass   the root class
     * @param builder builds runners for classes in the suite
     */
    public ParallelSuiteRunner(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass);
        Suite.SuiteClasses[] suites = getSuites(klass);
        final Map<Runner, Integer> runners = new HashMap<>();
        for (int i = 0; i < suites.length; i++) {
            final int lane = i;
            builder.runners(klass, Arrays.asList(suites[lane].value()))
                    .forEach(r -> runners.put(r, lane));
        }
        this.runners = Collections.unmodifiableMap(runners);
        setScheduler(new NaiveScheduler(suites.length));
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }



    private static Suite.SuiteClasses[] getSuites(Class<?> klass) throws InitializationError {
        final Suites annotation = klass.getAnnotation(Suites.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a Suites annotation", klass.getName()));
        }
        return annotation.value();
    }

    protected List<Runner> getChildren() {
        return Collections.unmodifiableList(new ArrayList<>(runners.keySet()));
    }

    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    static class RunNotifierRecorder extends RunNotifier {

        enum Method {
             addFirstListener
            ,addListener
            ,removeListener
            ,fireTestRunStarted
            ,fireTestRunFinished
            ,fireTestSuiteStarted
            ,fireTestSuiteFinished
            ,fireTestStarted
            ,fireTestFailure
            ,fireTestAssumptionFailed
            ,fireTestIgnored
            ,fireTestFinished
            ,pleaseStop
        }

        static class Event {
            final Method method;
            final Object parameter;
            Event(Method method, Object parameter) {
                this.method = method;
                this.parameter = parameter;
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
            record(Method.fireTestRunStarted,description);
        }

        @Override
        public void fireTestRunFinished(Result result) {
            System.out.println("notifier.fireTestRunFinished(result): " + result);
            record(Method.fireTestRunFinished,result);
        }

        @Override
        public void fireTestSuiteStarted(Description description) {
            System.out.println("notifier.fireTestSuiteStarted(description): " + description);
            record(Method.fireTestSuiteStarted,description);
        }

        @Override
        public void fireTestSuiteFinished(Description description) {
            System.out.println("notifier.fireTestSuiteFinished(description): " + description);
            record(Method.fireTestSuiteFinished,description);
        }

        @Override
        public void fireTestStarted(Description description) throws StoppedByUserException {
            System.out.println("notifier.fireTestStarted(description): " + description);
            record(Method.fireTestStarted,description);
        }

        @Override
        public void fireTestFailure(Failure failure) {
            System.out.println("notifier.fireTestFailure(failure): " + failure);
            record(Method.fireTestFailure,failure);
        }

        @Override
        public void fireTestAssumptionFailed(Failure failure) {
            System.out.println("notifier.fireTestAssumptionFailed(failure): " + failure);
            record(Method.fireTestAssumptionFailed,failure);
        }

        @Override
        public void fireTestIgnored(Description description) {
            System.out.println("notifier.fireTestIgnored(description): " + description);
            record(Method.fireTestIgnored,description);
        }

        @Override
        public void fireTestFinished(Description description) {
            System.out.println("notifier.fireTestFinished(description): " + description);
            record(Method.fireTestFinished,description);
        }

        @Override
        public void pleaseStop() {
            System.out.println("notifier.pleaseStop()");
            record(Method.pleaseStop, null);
        }

        private void record(Method method, Object parameter) {
            events.add(new Event(method,parameter));

        }


        protected void replay(RunNotifier notifier) {
           events.forEach( e -> replayEvent(notifier, e));
        }

        private void replayEvent(RunNotifier notifier, Event event) {
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


    protected void runChild(Runner runner, RunNotifier notifier) {
        System.out.format("%s: runChild: %s\n", Thread.currentThread(), runner.getDescription());
        final RunNotifierRecorder recorder = new RunNotifierRecorder();
        RuntimeException exception = null;
        try {
            runner.run(recorder);
        } catch (RuntimeException e) {
            exception = e;
        }
        synchronized (this) {
            recorder.replay(notifier);
            if (exception != null ) {
                throw exception;
            }

        }
    }


}
