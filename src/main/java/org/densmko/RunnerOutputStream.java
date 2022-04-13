package org.densmko;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RunnerOutputStream extends OutputStream {

    private final ByteArrayOutputStream general = new ByteArrayOutputStream();
    private final Map<ParallelRunnerClassLoader, ByteArrayOutputStream> streams = new ConcurrentHashMap<>();

    @Override
    public void write(byte[] b) throws IOException {
        final ParallelRunnerClassLoader cl = getContext();
        if (cl != null) {
            streams.computeIfAbsent(cl, t -> new ByteArrayOutputStream()).write(b);
        } else {
            general.write(b);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        final ParallelRunnerClassLoader cl = getContext();
        if (cl != null) {
            streams.computeIfAbsent(cl, t -> new ByteArrayOutputStream()).write(b, off, len);
        } else {
            general.write(b, off, len);
        }
    }

    @Override
    public void write(int b) throws IOException {
        final ParallelRunnerClassLoader cl = getContext();
        if (cl != null) {
            streams.computeIfAbsent(cl, t -> new ByteArrayOutputStream()).write(b);
        } else {
            general.write(b);
        }
    }

    public String getAndReset(Thread thread) {
        final ByteArrayOutputStream baos = streams.get(getContext(thread.getContextClassLoader()));
        if (baos != null) {
            // using default system encoding
            final String output = baos.toString();
            baos.reset();
            return output;
        } else {
            return null;
        }
    }

    public String getAndResetGeneral() {
            final String output = general.toString();
            general.reset();
            return output;

    }

    private ParallelRunnerClassLoader getContext() {
        return getContext(Thread.currentThread().getContextClassLoader());
    }

    private ParallelRunnerClassLoader getContext(ClassLoader loader) {
        if (loader instanceof ParallelRunnerClassLoader) {
            return (ParallelRunnerClassLoader) loader;
        } else {
            ClassLoader parent = loader.getParent();
            if (parent != null) {
                return getContext(parent);
            } else {
                return null;
            }
        }
    }



}
