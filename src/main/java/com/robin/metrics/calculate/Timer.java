package com.robin.metrics.calculate;


import com.robin.metrics.utils.CalculatorUtils;
import com.robin.metrics.utils.Clock;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 *
 **/

public abstract class Timer implements Calculator {

    protected String key;

    protected long creationTime;

    protected long reportTime;

    protected boolean recordZeroData = false;

    protected static final Clock clock = Clock.defaultClock();


    public Timer(String key) {
        this.key = key;
        this.creationTime = clock.getTime();
        this.reportTime = this.creationTime;
    }

    public void reset() {
        this.creationTime = clock.getTime();
        this.reportTime = this.creationTime;
    }


    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public long getCreationTime() {
        return this.creationTime;
    }


    @Override
    public long getReportTime() {
        return this.reportTime;
    }


    public boolean isRecordZeroData() {
        return this.recordZeroData;
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


    public List<ResultData> normalize(ResultData input, long interval) {

        List<ResultData> result = CalculatorUtils.normalizeTimeRange(input, interval);

        if ((result != null) && (!result.isEmpty())) {

            for (ResultData resultData : result) {
                resultData.setAvgTime(input.getAvgTime());
            }

        }//end if

        return result;
    }


    public static class Context implements Closeable {

        private final Timer timer;

        private final Clock _clock;

        private final long startTime;

        private Context(Timer _timer, Clock _clock) {
            this.timer = _timer;
            this._clock = _clock;
            this.startTime = _clock.getTime();
        }

        @Override
        public void close() throws IOException {
            this.stop();
        }

        public long stop() {
            long elapsed = this._clock.getTime() - this.startTime;
            this.timer.update(elapsed);
            return elapsed;
        }


    }

    public Context time() {
        return new Context(this, clock);
    }

}
