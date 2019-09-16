package com.robin.metrics.reportor;


import com.robin.metrics.channel.Channel;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public interface Reporter {

    public void setChannel(Channel channel);

    public void start();


}
