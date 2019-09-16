package com.robin.metrics.utils;

import com.robin.metrics.calculate.ResultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: Robin.li
 * @Date: 2018/7/27
 **/

public final class CalculatorUtils {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorUtils.class);

    private static final ThreadLocal<DecimalFormat> numberFormat = new ThreadLocal<DecimalFormat>() {

        protected DecimalFormat initialValue() {
            return new DecimalFormat("#.##");
        }
    };


    public static List<ResultData> normalizeTimeRange(ResultData input, long interval) {
        if ((input != null) && (interval > 0L) && (input.getStartTime() < input.getEndTime()) && (input.getStartTime() > 0L)) {
            long t0 = input.getStartTime() - input.getStartTime() % interval;
            if ((t0 == input.getStartTime()) && (input.getStartTime() + interval == input.getEndTime())) {
                return Collections.singletonList(input);
            }
            long modEnd = input.getEndTime() % interval;
            long tEnd = modEnd == 0L ? input.getEndTime() : input.getEndTime() - modEnd + interval;

            int n = (int) ((tEnd - t0) / interval);
            List<ResultData> result = new ArrayList<ResultData>(n);
            for (int i = 0; i < n; i++) {
                ResultData ri = new ResultData();
                ri.setMetricUniqueKey(input.getMetricUniqueKey());
                ri.setCalculateType(input.getCalculateType());
                ri.setStartTime(t0 + interval * i);
                ri.setEndTime(ri.getStartTime() + interval);
                result.add(ri);
            }
            return result;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Fail to normalize data for " + (input != null ? input.getMetricUniqueKey() : ""));
        }
        return Collections.emptyList();
    }

    public static String format(double number) {
        return ((DecimalFormat) numberFormat.get()).format(number);
    }


}
