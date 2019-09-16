package com.robin.metrics.calculate;

import com.robin.metrics.utils.LongAdder;

import java.util.Collection;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public class CounterImplLongAdder extends Counter {

    protected LongAdder count = new LongAdder();

    protected int zeroCounter = 0;


    public CounterImplLongAdder() {
        this("");
    }

    public CounterImplLongAdder(String key) {
        super(key);
    }


    @Override
    public void inc() {
        this.update(1L);
    }

    @Override
    public void inc(long value) {
        this.update(value);
    }

    @Override
    public void dec() {
        this.update(-1L);
    }

    @Override
    public void dec(long value) {

        this.update(-value);
    }

    @Override
    public void update(long value) {
        this.count.add(value);
    }

    @Override
    public void update(double value) {
        this.update(new Double(value).longValue());
    }

    @Override
    public synchronized int calculate(Collection<ResultData> datas) {

        int num = 0;
        if (datas == null) {
            return num;
        }
        long latestCount = this.count.sumThenReset();
        long currentTime = clock.getTime();
        if (!this.recordZeroData) {
            if (latestCount == 0L) {
                if (this.zeroCounter < Integer.MAX_VALUE) {
                    this.zeroCounter += 1;
                }
                if (this.zeroCounter > 1) {
                    this.reportTime = currentTime;
                    return num;
                }
            } else {
                if (this.zeroCounter > 1) {
                    ResultData result = new ResultData();
                    result.setCalculateType(1);
                    result.setMetricUniqueKey(this.key);
                    result.setCount(0L);
                    result.setCountRate(0.0D);

                    long rt = this.reportTime;
                    long elapsed = currentTime - rt;
                    result.setStartTime(rt - elapsed);
                    result.setEndTime(rt);

                    datas.add(result);
                    num++;
                }
                this.zeroCounter = 0;
            }
        }
        ResultData result = new ResultData();
        result.setCalculateType(1);
        result.setMetricUniqueKey(this.key);
        result.setCount(latestCount);

        long rt = this.reportTime;
        long elapsed = currentTime - rt;
        if (latestCount != 0L) {
            if (elapsed == 0L) {
                elapsed = 1L;
            }
            if (elapsed > 0L) {
                result.setCountRate(latestCount / (elapsed / 1000.0D));
            } else {
                result.setCountRate(latestCount / (elapsed / -1000.0D));
            }
            if (Double.isInfinite(result.getCountRate())) {
                result.setCountRate(Double.MAX_VALUE);
            }
        }
        result.setStartTime(currentTime - elapsed);
        result.setEndTime(currentTime);
        this.reportTime = currentTime;

        datas.add(result);
        num++;
        return num;
    }


    public synchronized void reset() {
        super.reset();
        this.count.reset();
    }
}
