import org.junit.Test;

import java.lang.management.ManagementFactory;

public class Foo {

    public void test(String name) {
        System.out.format("running %s:%s in pid: %s, thread: %s\n"
                , getClass()
                , name == null ? "test" : name
                , ManagementFactory.getRuntimeMXBean().getName()
                , Thread.currentThread());
    }

    @Test
    public void foo() {
        test("foo");
    }
}
