package com.robin.metrics.channel;

import com.robin.metrics.calculate.ResultData;

import java.util.Collection;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 **/

public interface Channel {

    public boolean offer(ResultData resultData);

    public ResultData poll();

    public int drainTo(Collection<? super ResultData> datas, int paramInt);

}
