package com.robin.metrics.calculate;



import com.robin.metrics.utils.CalculatorUtils;
import com.robin.metrics.utils.Clock;

import java.util.List;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 *
 **/

public abstract class Counter implements Calculator {

    protected static final Clock clock = Clock.defaultClock();

    protected String key;

    protected long creationTime;

    protected long reportTime;

    protected boolean recordZeroData = false;


    public Counter() {
        this("");
    }


    public Counter(String _key) {
        this.creationTime = clock.getTime();
        this.reportTime = this.creationTime;
        this.key = _key;
    }


    public abstract void inc();


    public abstract void inc(long value);

    public abstract void dec();

    public abstract void dec(long value);


    public List<ResultData> normalize(ResultData input, long interval) {

        List<ResultData> result = CalculatorUtils.normalizeTimeRange(input, interval);
        if ((result != null) && (!result.isEmpty())) {
            long density = input.getCount() / (input.getEndTime() - input.getStartTime());
            long countNew = 0L;
            for (ResultData ri : result) {
                long effectiveStart = Math.max(ri.getStartTime(), input.getStartTime());
                long effectiveEnd = Math.min(ri.getEndTime(), input.getEndTime());
                long effectiveDuration = effectiveEnd - effectiveStart;
                ri.setCount((density * effectiveDuration));
                ri.setCountRate(ri.getCount() / ((ri.getEndTime() - ri.getStartTime()) / 1000.0D));
                countNew += ri.getCount();
            }
            long countDelta = countNew - input.getCount();
            if ((countDelta != 0L) && (!result.isEmpty())) {
                ResultData ri = (ResultData) result.get(0);
                ri.setCount(ri.getCount() - countDelta);
                ri.setCountRate(ri.getCount() / ((ri.getEndTime() - ri.getStartTime()) / 1000.0D));
            }
        }
        return result;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setReportTime(long reportTime) {
        this.reportTime = reportTime;
    }

    public void setRecordZeroData(boolean recordZeroData) {
        this.recordZeroData = recordZeroData;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public long getReportTime() {
        return reportTime;
    }

    public boolean isRecordZeroData() {
        return recordZeroData;
    }


    public void reset(){
        this.creationTime = clock.getTime();
        this.reportTime = this.creationTime;
    }


}
