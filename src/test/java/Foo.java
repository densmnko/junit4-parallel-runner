import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class Foo {

    final private static AtomicInteger counter = new AtomicInteger(0);
    public static final long SLEEP_MILLIS = 500L;

    public void test(String name, boolean result) {
        int id = counter.incrementAndGet();
        System.out.format("%d: %s:%s, %s, thread: %s\n"
                , id
                , getClass()
                , name == null ? "test" : name
                , ManagementFactory.getRuntimeMXBean().getName()
                , Thread.currentThread());
        try {
            Thread.sleep(SLEEP_MILLIS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(name + ": assertion", result);
        System.out.println(id + ": done");
    }

    @Test
    public void foo() throws InterruptedException {
        Thread thread = new Thread( () -> {
            System.out.println("Have to capture this! " + Thread.currentThread() + ":" + Thread.currentThread().getThreadGroup());
        });
        thread.start();
        thread.join();
        test("foo", true);
    }

    @Test
    public void foofoo() {
        test("foofoo", false);
    }
}
