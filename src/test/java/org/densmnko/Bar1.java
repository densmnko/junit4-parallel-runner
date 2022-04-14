package org.densmnko;

import org.junit.Test;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Bar1 {

    static {
        System.out.println("org.densmko.Bar1.class static init");
    }

    final private static AtomicInteger counter = new AtomicInteger(0);
    public static final long SLEEP_MILLIS = 250L;

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
        assertEquals("Foo1", ClassWithStatic.value);
        assertTrue(name + ": assertion", result);
        System.out.println(id + ": done");
    }



    @Test
    public void bar() {
        test("bar", true);
    }

    @Test
    public void foobar() {
        test("foobar", true);

    }

    @Test
    public void barbar() {
        test("barbar", true);
    }

}
