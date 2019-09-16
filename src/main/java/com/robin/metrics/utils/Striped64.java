package com.robin.metrics.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Random;

/**
 * @Author: Robin.li
 * @Date: 2018/7/26
 **/

public abstract class Striped64 extends Number {


    static final ThreadHashCode threadHashCode = new ThreadHashCode();


    volatile transient Cell[] cells;

    volatile transient long base;

    volatile transient int busy;

    private static final Unsafe UNSAFE;

    private static final long baseOffset;

    private static final long busyOffset;


    static {
        try {
            UNSAFE = getUnsafe();
            Class<?> sk = Striped64.class;
            baseOffset = UNSAFE.objectFieldOffset(sk.getDeclaredField("base"));

            busyOffset = UNSAFE.objectFieldOffset(sk.getDeclaredField("busy"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }


    static final class Cell {

        volatile long p0;
        volatile long p1;
        volatile long p2;
        volatile long p3;
        volatile long p4;
        volatile long p5;
        volatile long p6;
        volatile long value;
        volatile long q0;
        volatile long q1;
        volatile long q2;
        volatile long q3;
        volatile long q4;
        volatile long q5;
        volatile long q6;
        private static final Unsafe UNSAFE;
        private static final long valueOffset;

        Cell(long x) {
            this.value = x;
        }

        final boolean cas(long cmp, long val) {
            return UNSAFE.compareAndSwapLong(this, valueOffset, cmp, val);
        }


        static {

            try {
                UNSAFE = Striped64.getUnsafe();
                Class<?> ak = Cell.class;
                valueOffset = UNSAFE.objectFieldOffset(ak.getDeclaredField("value"));
            } catch (Exception e) {
                throw new Error(e);
            }

        }
    }


    static final class HashCode {


        static final Random rng = new Random();
        int code;

        HashCode() {

            int h = rng.nextInt();
            this.code = (h == 0 ? 1 : h);
        }

    }


    static final class ThreadHashCode extends ThreadLocal<HashCode> {


        @Override
        public Striped64.HashCode initialValue() {
            return new Striped64.HashCode();
        }
    }


    static final int NCPU = Runtime.getRuntime().availableProcessors();


    final boolean casBase(long cmp, long val) {

        return UNSAFE.compareAndSwapLong(this, baseOffset, cmp, val);
    }

    final boolean casBusy() {

        return UNSAFE.compareAndSwapInt(this, busyOffset, 0, 1);
    }


    final void retryUpdate(long x, HashCode hc, boolean wasUncontended) {
        int h = hc.code;
        boolean collide = false;
        for (; ; ) {
            Cell[] as;
            int n;
            if (((as = this.cells) != null) && ((n = as.length) > 0)) {
                Cell a;
                if ((a = as[(n - 1 & h)]) == null) {
                    if (this.busy == 0) {
                        Cell r = new Cell(x);
                        if ((this.busy == 0) && (casBusy())) {
                            boolean created = false;
                            try {
                                Cell[] rs;
                                int m;
                                int j;
                                if (((rs = this.cells) != null) && ((m = rs.length) > 0) && (rs[(j = m - 1 & h)] == null)) {
                                    rs[j] = r;
                                    created = true;
                                }
                            } finally {
                                this.busy = 0;
                            }
                            if (!created) {
                                continue;
                            }
                            break;
                        }
                    }
                    collide = false;
                } else if (!wasUncontended) {
                    wasUncontended = true;
                } else {
                    long v;
                    if (a.cas(v = a.value, fn(v, x))) {
                        break;
                    }
                    if ((n >= NCPU) || (this.cells != as)) {
                        collide = false;
                    } else if (!collide) {
                        collide = true;
                    } else if ((this.busy == 0) && (casBusy())) {
                        try {
                            if (this.cells == as) {
                                Cell[] rs = new Cell[n << 1];
                                for (int i = 0; i < n; i++) {
                                    rs[i] = as[i];
                                }
                                this.cells = rs;
                            }
                        } finally {
                            this.busy = 0;
                        }
                        collide = false;
                        continue;
                    }
                }
                h ^= h << 13;
                h ^= h >>> 17;
                h ^= h << 5;
            } else if ((this.busy == 0) && (this.cells == as) && (casBusy())) {
                boolean init = false;
                try {
                    if (this.cells == as) {
                        Cell[] rs = new Cell[2];
                        rs[(h & 0x1)] = new Cell(x);
                        this.cells = rs;
                        init = true;
                    }
                } finally {
                    this.busy = 0;
                }
                if (init) {
                    break;
                }
            } else {
                long v;
                if (casBase(v = this.base, fn(v, x))) {
                    break;
                }
            }
        }
        hc.code = h;
    }


    final void internalReset(long initialValue) {
        Cell[] as = this.cells;
        this.base = initialValue;
        if (as != null) {
            int n = as.length;
            for (int i = 0; i < n; i++) {
                Cell a = as[i];
                if (a != null) {
                    a.value = initialValue;
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    private static Unsafe getUnsafe() {

        try {

            return Unsafe.getUnsafe();
        } catch (SecurityException ex) {


            try {
                PrivilegedExceptionAction action = new PrivilegedExceptionAction() {
                    @Override
                    public Unsafe run() throws Exception {
                        Class<Unsafe> k = Unsafe.class;
                        for (Field f : k.getDeclaredFields()) {
                            f.setAccessible(true);
                            Object x = f.get(null);
                            if (k.isInstance(x)) {
                                return (Unsafe) k.cast(x);
                            }
                        }
                        throw new NoSuchFieldError("the Unsafe");
                    }
                };

                return (Unsafe) AccessController.doPrivileged(action);
            } catch (PrivilegedActionException e) {
                throw new RuntimeException("Could not initialize intrinsics", e.getCause());
            }
        }

    }

    abstract long fn(long paramLong1, long paramLong2);
}
