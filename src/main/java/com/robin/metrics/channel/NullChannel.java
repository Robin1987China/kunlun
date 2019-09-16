package com.robin.metrics.channel;


import com.robin.metrics.calculate.ResultData;

import java.util.Collection;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public final class NullChannel implements Channel {

    private static NullChannel inst = new NullChannel();


    public static NullChannel get(){

        return inst;
    }


    @Override
    public boolean offer(ResultData resultData) {

        return true;
    }

    @Override
    public ResultData poll() {
        return null;
    }

    @Override
    public int drainTo(Collection<? super ResultData> datas, int paramInt) {
        return 0;
    }
}
