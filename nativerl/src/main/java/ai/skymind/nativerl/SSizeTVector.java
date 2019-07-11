// Targeted by JavaCPP version 1.5.1: DO NOT EDIT THIS FILE

package ai.skymind.nativerl;

import java.nio.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.annotation.*;

import static ai.skymind.nativerl.NativeRL.*;

@Name("std::vector<ssize_t>") @Properties(inherit = ai.skymind.nativerl.NativeRLPresets.class)
public class SSizeTVector extends Pointer {
    static { Loader.load(); }
    /** Pointer cast constructor. Invokes {@link Pointer#Pointer(Pointer)}. */
    public SSizeTVector(Pointer p) { super(p); }
    public SSizeTVector(long ... array) { this(array.length); put(array); }
    public SSizeTVector()       { allocate();  }
    public SSizeTVector(long n) { allocate(n); }
    private native void allocate();
    private native void allocate(@Cast("size_t") long n);
    public native @Name("operator=") @ByRef SSizeTVector put(@ByRef SSizeTVector x);

    public boolean empty() { return size() == 0; }
    public native long size();
    public void clear() { resize(0); }
    public native void resize(@Cast("size_t") long n);

    @Index(function = "at") public native @Cast("ssize_t") long get(@Cast("size_t") long i);
    public native SSizeTVector put(@Cast("size_t") long i, long value);

    public native @ByVal Iterator insert(@ByVal Iterator pos, @Cast("ssize_t") long value);
    public native @ByVal Iterator erase(@ByVal Iterator pos);
    public native @ByVal Iterator begin();
    public native @ByVal Iterator end();
    @NoOffset @Name("iterator") public static class Iterator extends Pointer {
        public Iterator(Pointer p) { super(p); }
        public Iterator() { }

        public native @Name("operator++") @ByRef Iterator increment();
        public native @Name("operator==") boolean equals(@ByRef Iterator it);
        public native @Name("operator*") @Cast("ssize_t") long get();
    }

    public long[] get() {
        long[] array = new long[size() < Integer.MAX_VALUE ? (int)size() : Integer.MAX_VALUE];
        for (int i = 0; i < array.length; i++) {
            array[i] = get(i);
        }
        return array;
    }
    @Override public String toString() {
        return java.util.Arrays.toString(get());
    }

    public long pop_back() {
        long size = size();
        long value = get(size - 1);
        resize(size - 1);
        return value;
    }
    public SSizeTVector push_back(long value) {
        long size = size();
        resize(size + 1);
        return put(size, value);
    }
    public SSizeTVector put(long value) {
        if (size() != 1) { resize(1); }
        return put(0, value);
    }
    public SSizeTVector put(long ... array) {
        if (size() != array.length) { resize(array.length); }
        for (int i = 0; i < array.length; i++) {
            put(i, array[i]);
        }
        return this;
    }
}

