import org.junit.Test;

public class Bar extends Foo {

    @Test
    public void bar() {
        test("bar", true);
    }

    @Test
    public void foobar() throws InterruptedException {
        Thread.sleep(500L);
        test("foobar throws", true);
    }

}
