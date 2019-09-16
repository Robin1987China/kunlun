package com.robin.metrics.executor;

import com.robin.metrics.channel.Channel;
import com.robin.metrics.registry.MetricsRegistry;

/**
 * @Author: Robin.li
 * @Date: 2018/7/26
 **/

public interface MetricProcessor {

    public void setRegistry(MetricsRegistry registry);


    public void setChannel(Channel channel);


    public void start();


}
