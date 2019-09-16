package com.robin.metrics.calculate;


import com.robin.metrics.utils.LongAdder;

import java.util.Collection;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 *
 **/

public class TimerImplLongAdder extends Timer {


    private final LongAdder size;

    private final LongAdder counter;

    private int zeroCounter = 0;


    public TimerImplLongAdder() {

        this("");
    }


    public TimerImplLongAdder(String _key) {
        super(_key);
        this.size = new LongAdder();
        this.counter = new LongAdder();
    }


    @Override
    public void update(long value) {


        if (value >= 0L) {
            this.size.add(1L);
            this.counter.add(value);
        }

    }

    @Override
    public void update(double value) {

        this.update(new Double(value).longValue());

    }


    public synchronized void reset() {
        super.reset();
        this.size.reset();
        this.counter.reset();
    }

    @Override
    public synchronized int calculate(Collection<ResultData> datas) {


        int num = 0;
        if (datas == null) {
            return num;
        }
        long currentTime = clock.getTime();
        long c = this.counter.sumThenReset();
        long s = this.size.sumThenReset();
        if (!this.recordZeroData) {
            if (s == 0L) {
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
                    result.setCalculateType(2);
                    result.setMetricUniqueKey(this.key);
                    result.setAvgTime(0.0D);

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
        result.setCalculateType(2);
        result.setMetricUniqueKey(this.key);
        result.setAvgTime(s == 0L ? c : c / s);
        result.setStartTime(this.reportTime);
        result.setEndTime(currentTime);
        this.reportTime = currentTime;

        datas.add(result);
        num++;
        return num;
    }
}
