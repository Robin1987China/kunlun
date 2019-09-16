package com.robin.metrics.calculate;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 *
 **/

public class GaugeForDouble extends Gauge {

    protected AtomicLong valueDouble = new AtomicLong(0L);


    public GaugeForDouble() {
        this("");
    }

    public GaugeForDouble(String _key) {

        super(_key);

    }


    @Override
    public void update(long value) {

        this.update(new Double(value).longValue());

    }

    @Override
    public void update(double value) {
        long old = this.valueDouble.get();
        if (old != value) {
            this.valueDouble.compareAndSet(old, Double.doubleToRawLongBits(value));
        }
    }

    @Override
    protected Long loadValue() {
        return null;
    }

    @Override
    protected Double loadValue(boolean isDouble) {
        return null;
    }

    @Override
    public int calculate(Collection<ResultData> datas) {

        if (datas == null) {
            return 0;
        }
        Double newValDouble = loadValue(true);
        if (newValDouble != null) {
            update(newValDouble.doubleValue());
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
        result.setGaugeDouble(Double.longBitsToDouble(this.valueDouble.get()));
        if (Double.isInfinite(result.getGaugeDouble())) {
            result.setGaugeDouble(Double.MAX_VALUE);
        }
        if (Double.isNaN(result.getGaugeDouble())) {
            result.setGaugeDouble(0.0D);
        }
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
}
