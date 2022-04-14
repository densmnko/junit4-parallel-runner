package org.densmnko;

import java.net.URL;
import java.net.URLClassLoader;

class ParallelRunnerClassLoader extends URLClassLoader {


    private final int lane;
    private final ClassLoader parent;
    private final String[] isolate;

    public ParallelRunnerClassLoader(int lane, ClassLoader parent, String[] isolate) {
        super(((URLClassLoader)parent).getURLs(), null);
        this.lane = lane;
        this.parent = parent;
        this.isolate = isolate;
    }

    @Override
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        if ( isIsolated(name) ) {
            final Class<?> c = findLoadedClass(name);
            return c == null ? findClass(name) : c;
        }
        return parent.loadClass(name);
    }

    private boolean isIsolated(String name) {
        if (isolate != null && isolate.length > 0 ) {
            for ( String aPackage : isolate ) {
                if ( name.startsWith(aPackage)) {
                    return true;
                }
            }
        }
        return false;
    }

}
