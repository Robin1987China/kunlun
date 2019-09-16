package com.robin.metrics.utils;


import com.robin.metrics.calculate.ResultData;
import com.robin.metrics.encoding.TextCodec;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public final class MetricsDecodeUtils {

  private static  final TextCodec codec = new TextCodec();



  public static ResultData decodeMetrics(String msg){

      return codec.readObject(msg);
  }


    public static void main(String[] args) {
      ResultData resultData =  decodeMetrics("ct=1,muk=/qps_demo.app/service:service61/status:4xx,d=10000,c=54247,cr=5.4247,s=1487227510000");

        System.out.println(resultData);
  }



}
