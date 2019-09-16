package com.robin.metrics.calculate;

import com.robin.metrics.utils.LongAdder;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 *
 **/

public class SlidingWindowCounter extends Counter {


    private final int size;

    private final long precision;

    private final ConcurrentLinkedQueue<DataPoint> history;

    private final MutableDataPoint currentDataPoint;

    private final AtomicLong count;


    public SlidingWindowCounter(String key, int size, long precision, TimeUnit precisionUnit) {
        super(key);
        if (precision < 1L) {
            throw new IllegalArgumentException("Precision should be positive.");
        }
        this.key = key;
        this.size = size;
        this.precision = precisionUnit.toMillis(precision);

        this.history = new ConcurrentLinkedQueue<DataPoint>();
        this.count = new AtomicLong();

        long ts = clock.getTime();
        ts -= ts % precision;
        this.currentDataPoint = new MutableDataPoint(ts);
    }


    @Override
    public void inc() {
        this.update(1l);
    }

    @Override
    public void inc(long value) {
        this.update(value);
    }

    @Override
    public void dec() {
        this.update(-1l);
    }

    @Override
    public void dec(long value) {
        this.update(-value);
    }

    @Override
    public void update(long value) {

        long ts = clock.getTime();
        ts -= ts % this.precision;

        MutableDataPoint dp = this.currentDataPoint;
        long tsCur = dp.getTimestamp();
        if (ts <= tsCur) {
            dp.getAdder().add(value);
        } else {
            DataPoint snapshot = dp.snapshotOrUpdate(ts, value);
            if (snapshot != null) {
                this.history.add(snapshot);
                countAndTrim();
            }
        }

    }

    @Override
    public void update(double value) {
        this.update(new Double(value).longValue());
    }


    public ResultData pollHistory() {
        ResultData result = null;

        DataPoint dp = (DataPoint) this.history.poll();
        if (dp != null) {
            this.count.decrementAndGet();
            this.reportTime = clock.getTime();

            result = new ResultData();
            result.setMetricUniqueKey(this.key);
            result.setCalculateType(1);
            result.setCount(dp.getValue());
            result.setCountRate(result.getCount() / (this.precision / 1000.0D));
            if (Double.isInfinite(result.getCountRate())) {
                result.setCountRate(Double.MAX_VALUE);
            }
            result.setStartTime(dp.getTimestamp());
            result.setEndTime(result.getStartTime() + this.precision);
        }
        return result;
    }


    @Override
    public int calculate(Collection<ResultData> datas) {
        int num = 0;
        boolean active = false;
        for (; ; ) {
            ResultData res = pollHistory();
            if (res == null) {
                break;
            }
            active = true;
            if ((res.getCount() != 0L) || (this.recordZeroData)) {
                datas.add(res);
                num++;
            }
        }
        if (!active) {
            update(0L);
            for (; ; ) {
                ResultData res = pollHistory();
                if (res == null) {
                    break;
                }
                if ((res.getCount() != 0L) || (this.recordZeroData)) {
                    datas.add(res);
                    num++;
                }
            }
        }

        return num;
    }

    private void countAndTrim() {
        if (this.size > 0) {
            long delta = this.count.addAndGet(1L) - this.size;
            if (delta > 0L) {
                for (int i = 0; i < delta; i++) {
                    if (this.history.poll() == null) {
                        break;
                    }

                    if (i > 0) {
                        this.count.addAndGet(-i);
                    }
                }

            }
        }
    }


    private static class MutableDataPoint {

        private volatile long timestamp;

        private LongAdder adder;


        public MutableDataPoint(long timestamp) {
            this.timestamp = timestamp;
            adder = new LongAdder();
        }


        public synchronized SlidingWindowCounter.DataPoint snapshotOrUpdate(long timestamp, long value) {
            long old = this.timestamp;
            if (timestamp > old) {
                SlidingWindowCounter.DataPoint dp = new SlidingWindowCounter.DataPoint(old, this.adder.sumThenReset());
                this.timestamp = timestamp;
                this.adder.add(value);
                return dp;
            }
            this.adder.add(value);
            return null;
        }


        public long getTimestamp() {
            return this.timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public LongAdder getAdder() {
            return this.adder;
        }

    }


    private static class DataPoint {

        private long timestamp;

        private long value;

        public DataPoint(long timestamp, long value) {

            this.timestamp = timestamp;
            this.value = value;
        }


        public long getTimestamp() {
            return timestamp;
        }

        public long getValue() {
            return value;
        }

    }
}
