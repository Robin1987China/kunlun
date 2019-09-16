package com.robin.metrics.channel;


import com.robin.metrics.calculate.ResultData;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public class BoundedChannel implements Channel {


    private LinkedBlockingQueue<ResultData> queue;

    public BoundedChannel(int _capacity) {
        this.queue = new LinkedBlockingQueue<>(_capacity);
    }


    @Override
    public boolean offer(ResultData resultData) {
        return this.queue.offer(resultData);
    }

    @Override
    public ResultData poll() {
        return (ResultData) this.queue.poll();
    }

    @Override
    public int drainTo(Collection<? super ResultData> datas, int paramInt) {
        return this.queue.drainTo(datas, 10);
    }
}
