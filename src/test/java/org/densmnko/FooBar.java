package org.densmnko;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(ParallelSuiteRunner.class)
@ParallelSuiteRunner.Suites(
        isolate = {"org.densmnko"},
        value = {
        @Suite.SuiteClasses({ Foo.class, Bar.class, Foo.class })
        ,@Suite.SuiteClasses({ Foo1.class, Bar1.class, Bar1.class })
})
public class FooBar {

}



