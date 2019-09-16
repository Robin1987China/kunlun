package com.robin.metrics.utils;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 **/

public class ThreadLocalStringBuilder extends ThreadLocal<StringBuilder>{

    private int capacity;

    public ThreadLocalStringBuilder(){
        this(100);
    }


    public ThreadLocalStringBuilder(int capacity){

        if (capacity<0)
                throw new IllegalArgumentException("Invalid capacity "+capacity);

        this.capacity = capacity;
    }



    @Override
    protected StringBuilder initialValue() {
        return new StringBuilder(this.capacity);
    }
}
