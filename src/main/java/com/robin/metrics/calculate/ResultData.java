package com.robin.metrics.calculate;

import java.util.Map;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 *
 **/

public class ResultData {

    private int calculateType;
    private String metricUniqueKey;
    private long startTime;
    private long endTime;
    private long count;
    private double countRate;
    private double avgTime;
    private long gauge;
    private double gaugeDouble;
    private String namespace;
    private String metricName;
    private Map<String, String> tags;

    public int getCalculateType() {
        return this.calculateType;
    }

    public void setCalculateType(int calculateType) {
        this.calculateType = calculateType;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public double getCountRate() {
        return this.countRate;
    }

    public void setCountRate(double countRate) {
        this.countRate = countRate;
    }

    public double getAvgTime() {
        return this.avgTime;
    }

    public void setAvgTime(double avgTime) {
        this.avgTime = avgTime;
    }

    public long getGauge() {
        return this.gauge;
    }

    public void setGauge(long gauge) {
        this.gauge = gauge;
    }

    public double getGaugeDouble() {
        return this.gaugeDouble;
    }

    public void setGaugeDouble(double gaugeDouble) {
        this.gaugeDouble = gaugeDouble;
    }

    public String getMetricUniqueKey() {
        return this.metricUniqueKey;
    }

    public void setMetricUniqueKey(String metricUniqueKey) {
        this.metricUniqueKey = metricUniqueKey;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getMetricName() {
        return this.metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
