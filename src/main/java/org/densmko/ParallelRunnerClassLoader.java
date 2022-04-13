package org.densmko;

class ParallelRunnerClassLoader extends ClassLoader {

    public ParallelRunnerClassLoader(ClassLoader parent) {
        super(parent);
    }
    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
      return super.findClass(name);
    }

}
