import org.densmko.ParallelSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(ParallelSuite.class)
@ParallelSuite.Suites({
        @Suite.SuiteClasses({ Foo.class, Bar.class })
})
public class ParallelTestFooBar {

}
