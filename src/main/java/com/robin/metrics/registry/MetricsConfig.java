package com.robin.metrics.registry;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 **/

public class MetricsConfig {

    private int slidingWindowCounterSize = 60;
    private int slidingWindowCounterPrecision = 1000;
    private long slidingWindowCounterPollingInterval = 10000L;
    private long pollingInterval = 10000L;
    private int channelCapacity = 1000;
    private String loggerReporterName = "kunlun-metric-logger";


    public int getSlidingWindowCounterSize() {
        return this.slidingWindowCounterSize;
    }

    public void setSlidingWindowCounterSize(int slidingWindowCounterSize) {
        this.slidingWindowCounterSize = slidingWindowCounterSize;
    }

    public int getSlidingWindowCounterPrecision() {
        return this.slidingWindowCounterPrecision;
    }

    public void setSlidingWindowCounterPrecision(int slidingWindowCounterPrecision) {
        if (slidingWindowCounterPrecision <= 0) {
            throw new IllegalArgumentException("Invalid slidingWindowCounterPrecision " + slidingWindowCounterPrecision);
        }
        this.slidingWindowCounterPrecision = slidingWindowCounterPrecision;
    }

    public long getSlidingWindowCounterPollingInterval() {
        return this.slidingWindowCounterPollingInterval;
    }

    public void setSlidingWindowCounterPollingInterval(long value) {
        if (value < 1L) {
            throw new IllegalArgumentException("Invalid slidingWindowCounterPollingInterval " + value);
        }
        this.slidingWindowCounterPollingInterval = value;
    }

    public long getPollingInterval() {
        return this.pollingInterval;
    }

    public void setPollingInterval(long pollingInterval) {
        if (pollingInterval < 1L) {
            throw new IllegalArgumentException("Invalid pollingInterval " + pollingInterval);
        }
        this.pollingInterval = pollingInterval;
    }

    public int getChannelCapacity() {
        return this.channelCapacity;
    }

    public void setChannelCapacity(int channelCapacity) {
        if (channelCapacity < 1) {
            throw new IllegalArgumentException("Invalid channelCapacity " + channelCapacity);
        }
        this.channelCapacity = channelCapacity;
    }

    public String getLoggerReporterName() {
        return this.loggerReporterName;
    }

    public void setLoggerReporterName(String loggerReporterName) {
        if ((loggerReporterName == null) || (loggerReporterName.isEmpty())) {
            throw new IllegalArgumentException("loggerReporterName should not be empty");
        }
        this.loggerReporterName = loggerReporterName;
    }

    public String toString() {
        StringBuffer toString = new StringBuffer();
        toString.append(100).append("slidingWindowCounterSize = ").append(this.slidingWindowCounterSize).append(" ; slidingWindowCounterPrecision = ")
                 .append(this.slidingWindowCounterPrecision).append(" ; pollingInterval = ").append(this.pollingInterval).append(" ; channelCapacity = ").append(this.channelCapacity)
                .append(" ; loggerReporterName = ").append(this.loggerReporterName);

        return toString.toString();
    }
}
