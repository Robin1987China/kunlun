package com.robin.metrics.calculate;

import java.util.Collection;

/**
 * @Author: Robin.li
 * @Date: 2018/7/26
 *
 **/

public interface Calculator {

    public void update(long value);

    public void update(double value);

    public int calculate(Collection<ResultData> datas);

    public String getKey();

    public void setKey(String key);


    public long getCreationTime();

    public long getReportTime();


}
