package com.robin.metrics.reportor;

import com.robin.metrics.calculate.ResultData;
import com.robin.metrics.channel.Channel;
import com.robin.metrics.channel.NullChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public abstract class ScheduledReporter implements Reporter {


    private static final Logger logger = LoggerFactory.getLogger(ScheduledReporter.class);

    private final ExecutorService executor;

    private static final AtomicInteger FACTORY_ID = new AtomicInteger();

    protected Channel channel;

    protected int state = 0;

    protected ScheduledReporter(String name) {
        this(Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name + '-' + FACTORY_ID.incrementAndGet())), NullChannel.get());
    }

    protected ScheduledReporter(ScheduledExecutorService executor, Channel channel) {
        if (executor == null) {
            throw new IllegalArgumentException("executor should not be empty");
        }
        if (channel == null) {
            throw new IllegalArgumentException("channel should not be empty");
        }
        this.executor = executor;
        this.channel = channel;
    }


    @Override
    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public synchronized void start() {


        if (this.state == 0) {
            this.executor.execute(new Runnable() {
                public void run() {
                    try {
                        ScheduledReporter.this.report();
                    } catch (Throwable t) {
                        ScheduledReporter.logger.error("Exception was suppressed.", t);
                    }
                }
            });
            this.state = 1;
        }


    }

    public void stop() {
        this.executor.shutdown();
        try {
            if (!this.executor.awaitTermination(1L, TimeUnit.SECONDS)) {
                this.executor.shutdownNow();
                if (!this.executor.awaitTermination(1L, TimeUnit.SECONDS)) {
                    logger.error(getClass().getSimpleName() + ": ScheduledExecutorService did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            this.executor.shutdownNow();

            Thread.currentThread().interrupt();
        }
    }

    public void report() {
        List<ResultData> resultDatas = new ArrayList<ResultData>(10);
        int count = 0;
        for (; ; ) {
            Channel ch = this.channel;
            if (ch != null) {
                ch.drainTo(resultDatas, 10);
            }
            if (resultDatas.size() > 0) {
                report(resultDatas);
                resultDatas.clear();
            } else {
                count++;
                if (count >= 100) {
                    try {
                        Thread.sleep(10L);
                        count = 0;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Reporter is interrupted", e);
                        }
                    }
                }
            }
        }
    }

    public abstract void report(List<ResultData> paramList);


    private static class NamedThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private NamedThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
            this.namePrefix = ("metrics-" + name + "-thread-");
        }

        @Override
        public Thread newThread(Runnable r) {

            Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
            t.setDaemon(true);
            if (t.getPriority() != 5) {
                t.setPriority(5);
            }
            return t;
        }
    }
}
