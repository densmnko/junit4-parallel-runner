package org.densmko;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * todo
 */
public class ParallelSuite extends ParentRunner<Runner> {

    private final Suite.SuiteClasses[] suites;
    private final List<Runner> runners;

    /**
     * The <code>Suites</code> annotation specifies the list of <code>SuiteClasses</code> to be run in parallel when a class
     * annotated with <code>@RunWith(ParallelSuite.class)</code> is run.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface Suites {
        /**
         * @return the classes to be run
         */
        Suite.SuiteClasses[] value();
    }

    /**
     * Called reflectively on classes annotated with <code>@RunWith(ParallelSuite.class)</code>
     *
     * @param klass the root class
     * @param builder builds runners for classes in the suite
     */
    public ParallelSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass);
        suites = getSuites(klass);
        runners = Collections.unmodifiableList(builder.runners(klass, Arrays.stream(suites)
                .map(Suite.SuiteClasses::value)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList())));
    }

    private static Suite.SuiteClasses[] getSuites(Class<?> klass) throws InitializationError {
        final Suites annotation = klass.getAnnotation(Suites.class);
        if (annotation == null) {
            throw new InitializationError(String.format("class '%s' must have a Suites annotation", klass.getName()));
        }
        return annotation.value();
    }

    protected List<Runner> getChildren() {
        return runners;
    }

    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    protected void runChild(Runner runner, RunNotifier notifier) {
        runner.run(notifier);
    }
}
