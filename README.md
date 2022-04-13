# junit4-parallel-runner
JUnit4 runner to run tests in parallel threads or even processes 

The goal is to run legacy JUnit4 tests in parallel with 'IDEA Project', Teamcity 'IntelliJ IDEA Project' build with minimum efforts.

Sample usage: 
```java
@RunWith(ParallelSuiteRunner.class)
@ParallelSuiteRunner.Suites({
      @Suite.SuiteClasses({ Foo.class, Bar.class, Foo.class })
    , @Suite.SuiteClasses({ Foo.class, Bar.class, Bar.class })
})
public class FooBar {
}
```

Each ```@Suite.SuiteClasses``` will run in parallel, in separate thread and class loader.

BTW, Yes, I know about JUnit5 and maven surefire plugin )) 
