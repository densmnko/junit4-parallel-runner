import org.densmko.ParallelSuiteRunner;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(ParallelSuiteRunner.class)
@ParallelSuiteRunner.Suites({
        @Suite.SuiteClasses({ Foo.class, Bar.class, Foo.class })
        ,@Suite.SuiteClasses({ Foo.class, Bar.class, Bar.class })
})
public class FooBar {

}



