package com.robin.metrics.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * @Author: Robin.li
 * @Date: 2018/7/26
 **/

public abstract class Clock {

   public abstract long getTick();

   public long  getTime(){
        return System.currentTimeMillis();
   }


   private static final Clock DEFAULT = new UserTimeClock();

   public static Clock defaultClock(){

       return DEFAULT;
   }

   public static class UserTimeClock extends Clock {

       @Override
       public long getTick() {
           return System.nanoTime();
       }
   }


   public static class CpuTimeClock extends Clock {

    private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();


       @Override
       public long getTick() {
           return THREAD_MX_BEAN.getCurrentThreadCpuTime();
       }
   }

}
