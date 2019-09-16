package com.robin.metrics.executor;


import com.robin.metrics.calculate.Calculator;
import com.robin.metrics.calculate.ResultData;
import com.robin.metrics.calculate.SlidingWindowCounter;
import com.robin.metrics.channel.Channel;
import com.robin.metrics.registry.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 **/

public class ExecutorPool implements MetricProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorPool.class);

    private final ScheduledExecutorService es;


    private MetricsRegistry registry;

    private Channel channel;

    private long interval = 10000L;

    private long intervalSlidingWindowCounter = 10000L;

    private int state = 0;


    public ExecutorPool(MetricsRegistry registry, Channel channel, long interval, long intervalSlidingWindowCounter) {

        if (registry == null) {
            throw new IllegalArgumentException("registry should not be empty.");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel should not be empty.");
        }
        if (interval < 1L) {
            throw new IllegalArgumentException("Invalid interval " + interval);
        }
        if (intervalSlidingWindowCounter < 1L) {
            throw new IllegalArgumentException("Invalid intervalSlidingWindowCounter " + intervalSlidingWindowCounter);
        }
        this.registry = registry;
        this.channel = channel;
        this.interval = interval;
        this.intervalSlidingWindowCounter = intervalSlidingWindowCounter;

        this.es = Executors.newScheduledThreadPool(1);
    }


    @Override
    public void setRegistry(MetricsRegistry registry) {
        if (registry != null)
            this.registry = registry;
    }

    @Override
    public void setChannel(Channel channel) {

        if (channel != null)
            this.channel = channel;
    }

    @Override
    public synchronized void start() {
        if (this.state == 0) {
            this.es.scheduleWithFixedDelay(new Executor(false), 0L, this.interval, TimeUnit.MILLISECONDS);
            this.es.scheduleWithFixedDelay(new Executor(true), 10L, this.intervalSlidingWindowCounter, TimeUnit.MILLISECONDS);
            this.state = 1;
        }
    }

    private void output(List<ResultData> data) {
        Channel ch = this.channel;
        if ((data != null) && (!data.isEmpty()) && (ch != null)) {
            for (ResultData d : data) {
                boolean rc = ch.offer(d);
                if ((!rc) && (logger.isDebugEnabled())) {
                    logger.debug("Channel is full and drop data: " + d.getMetricUniqueKey());
                }
            }
        }
    }

    private class Executor implements Runnable {
        private boolean slidingWindowCounterOnly = false;

        public Executor(boolean slidingWindowCounterOnly) {
            this.slidingWindowCounterOnly = slidingWindowCounterOnly;
        }

        public void run() {
            MetricsRegistry reg = ExecutorPool.this.registry;
            if (reg == null) {
                return;
            }
            long ts1 = System.currentTimeMillis();
            long count = 0L;
            try {
                if (!this.slidingWindowCounterOnly) {
                    reg.discoverMetrics();
                }
               List<ResultData> results = new ArrayList<ResultData>();
                for (Calculator it : reg) {
                    boolean isSliding = it instanceof SlidingWindowCounter;
                    if (isSliding == this.slidingWindowCounterOnly) {
                        try {
                            results.clear();
                            count += it.calculate(results);
                        } catch (Throwable t) {
                            if (ExecutorPool.logger.isDebugEnabled()) {
                                ExecutorPool.logger.debug("Fail to calculate metric for " + it.getKey());
                            }
                        } finally {
                            ExecutorPool.this.output(results);
                            results.clear();
                        }
                    }
                }
            } catch (Throwable t) {
                List<ResultData> results;
                long ts2;
                if (ExecutorPool.logger.isDebugEnabled()) {
                    ExecutorPool.logger.debug("Ignore error and continue calculation.", t);
                }
            } finally {
                long ts2;
                if (ExecutorPool.logger.isDebugEnabled()) {
                    ts2 = System.currentTimeMillis();
                    ExecutorPool.logger.debug(100 + "Calculation:" + " slidingWindowCounterOnly = " + this.slidingWindowCounterOnly + " ; count = " + count + " ; start = " + ts1 + " ; end = " + ts2 + " ; duration = " + (ts2 - ts1));
                }
            }
        }
    }
}
