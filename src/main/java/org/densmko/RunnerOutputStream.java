package org.densmko;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RunnerOutputStream extends OutputStream {

    private final Map<Thread,ByteArrayOutputStream> streams = new ConcurrentHashMap<>();

    @Override
    public void write(byte[] b) throws IOException {
        streams.computeIfAbsent(Thread.currentThread(), t -> new ByteArrayOutputStream())
                .write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        streams.computeIfAbsent(Thread.currentThread(), t -> new ByteArrayOutputStream())
                .write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        streams.computeIfAbsent(Thread.currentThread(), t -> new ByteArrayOutputStream())
                .write(b);
    }

    public String getAndReset(Thread thread) {
        final ByteArrayOutputStream baos = streams.get(thread);
        if (baos != null ) {
            // using default system encoding
            final String output = baos.toString();
            baos.reset();
            return output;
        } else {
            return null;
        }
    }
}
