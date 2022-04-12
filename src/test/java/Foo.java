import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class Foo {

    final private static AtomicInteger counter = new AtomicInteger(0);

    public void test(String name, boolean result) {

        int id = counter.incrementAndGet();
        System.out.format("%d: %s:%s, %s, thread: %s\n"
                , id
                , getClass()
                , name == null ? "test" : name
                , ManagementFactory.getRuntimeMXBean().getName()
                , Thread.currentThread());
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(name + ": assertion", result);
        System.out.println(id + ": done");
    }

    @Test
    public void foo() {
        test("foo", true);
    }

    @Test
    public void foofoo() {
        test("foofoo", false);
    }
}
