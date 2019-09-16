package com.robin.metrics.registry;

import com.robin.metrics.channel.BoundedChannel;
import com.robin.metrics.channel.Channel;
import com.robin.metrics.executor.ExecutorPool;
import com.robin.metrics.executor.MetricProcessor;
import com.robin.metrics.reportor.LoggerReporter;
import com.robin.metrics.reportor.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public class MetricsRegistryBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MetricsRegistryBuilder.class);

    private MetricsConfig config;

    protected MetricsRegistryBuilder() {

        this.config = new MetricsConfig();
    }


    public static MetricsRegistryBuilder create() {
        return new MetricsRegistryBuilder();
    }


    public MetricsRegistry build() {

        MetricsRegistry reg = new MetricsRegistry(this.config);

        Channel channel = new BoundedChannel(this.config.getChannelCapacity());
        MetricProcessor processor = new ExecutorPool(reg, channel, this.config.getPollingInterval(), this.config.getSlidingWindowCounterPollingInterval());
        Reporter reporter = new LoggerReporter(this.config.getLoggerReporterName(), channel);
        processor.start();
        reporter.start();

        logger.info("create MetricsRegistry: " + reg.getConfig().toString());

        return reg;
    }


    public MetricsRegistryBuilder setSlidingWindowCounterSize(int value) {
        this.config.setSlidingWindowCounterSize(value);
        return this;
    }

    public MetricsRegistryBuilder setSlidingWindowCounterPrecision(int value) {
        this.config.setSlidingWindowCounterPrecision(value);
        return this;
    }

    public MetricsRegistryBuilder setSlidingWindowCounterPollingInterval(long value) {
        this.config.setSlidingWindowCounterPollingInterval(value);
        return this;
    }

    public MetricsRegistryBuilder setLoggerReporterName(String value) {
        this.config.setLoggerReporterName(value);
        return this;
    }

    public MetricsRegistryBuilder setPollingInterval(long value) {
        this.config.setPollingInterval(value);
        return this;
    }

    public MetricsRegistryBuilder setChannelCapacity(int value) {
        this.config.setChannelCapacity(value);
        return this;
    }

}
