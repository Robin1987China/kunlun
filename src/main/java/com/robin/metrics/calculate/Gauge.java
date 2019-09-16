package com.robin.metrics.calculate;

import com.robin.metrics.utils.Clock;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 *
 **/

public class Gauge implements Calculator {

    protected static Clock clock = Clock.defaultClock();

    protected String key;

    protected long creationTime;

    protected long reportTime;

    protected AtomicLong value = new AtomicLong(0L);


    public Gauge() {
        this("");
    }

    public Gauge(String _key) {
        this.key = _key;
        this.creationTime = clock.getTime();
        this.reportTime = creationTime;
    }

    @Override
    public void update(long value) {
        long old = this.value.get();
        if (old != value) {
            this.value.compareAndSet(old, value);
        }
    }


    @Override
    public void update(double value) {
        this.update(new Double(value).longValue());
    }

    protected Long loadValue() {
        return null;
    }

    protected Double loadValue(boolean isDouble) {
        return null;
    }

    @Override
    public int calculate(Collection<ResultData> datas) {

        if (datas == null)
            return 0;

        Long newVal = loadValue();
        if (newVal != null) {
            update(newVal.longValue());
        }

        long ts = clock.getTime();
        long rt = this.reportTime;
        this.reportTime = ts;
        long elapsed = ts - rt;
        if (elapsed <= 0L) {
            elapsed = 1000L;
        }
        ResultData result = new ResultData();
        result.setMetricUniqueKey(this.key);
        result.setCalculateType(3);
        result.setStartTime(ts - elapsed);
        result.setEndTime(ts);
        result.setGauge(this.value.get());
        datas.add(result);
        return 1;
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public long getCreationTime() {
        return 0L;
    }

    @Override
    public long getReportTime() {
        return 0L;
    }

    public static void main(String[] args) {
        String callee = "   ";
        String callee2 = "abc";
        System.out.println(callee.isEmpty());
        System.out.println(callee2.isEmpty());

    }
}
