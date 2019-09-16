package com.robin.metrics.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @Author: Robin.li
 * @Date: 2018/7/26
 **/

public class LongAdder extends Striped64 implements Serializable {

    private static final long serialiID = -22329832432l;

    final long fn(long v, long x) {

        return v + x;
    }


    public void add(long x) {

        Striped64.Cell[] as;
        long b;

        if (((as = this.cells) != null) || (!casBase(b = this.base, b + x))) {
            boolean uncontended = true;
            Striped64.HashCode hc;
            int h = (hc = (Striped64.HashCode) threadHashCode.get()).code;
            int n;
            Striped64.Cell a;
            long v;
            if ((as == null) || ((n = as.length) < 1) || ((a = as[(n - 1 & h)]) == null) || (!(uncontended = a.cas(v = a.value, v + x)))) {
                retryUpdate(x, hc, uncontended);
            }
        }
    }


    public void increment() {
        add(1l);
    }

    public void decrement() {
        add(-1l);
    }

    public long sum() {
        long sum = this.base;
        Striped64.Cell[] as = this.cells;
        if (as != null) {
            int n = as.length;
            for (int i = 0; i < n; i++) {
                Striped64.Cell a = as[i];
                if (a != null) {
                    sum += a.value;
                }
            }
        }
        return sum;
    }


    public void reset() {
        internalReset(0L);
    }


    @Override
    public int intValue() {
        return (int) sum();
    }

    @Override
    public long longValue() {
        return sum();
    }

    @Override
    public float floatValue() {
        return (float) sum();
    }

    @Override
    public double doubleValue() {
        return sum();
    }


    public long sumThenReset() {
        long sum = this.base;
        Striped64.Cell[] as = this.cells;
        this.base = 0L;
        if (as != null) {
            int n = as.length;
            for (int i = 0; i < n; i++) {
                Striped64.Cell a = as[i];
                if (a != null) {
                    sum += a.value;
                    a.value = 0L;
                }
            }
        }
        return sum;
    }

    @Override
    public String toString() {
        return Long.toString(sum());
    }


    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeLong(sum());
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.busy = 0;
        this.cells = null;
        this.base = s.readLong();
    }


}
