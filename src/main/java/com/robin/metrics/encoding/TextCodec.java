package com.robin.metrics.encoding;


import com.robin.metrics.calculate.ResultData;
import com.robin.metrics.utils.CalculatorUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public class TextCodec {


    private static final String EQUATE_SIGN = "=";
    private static final String COMMA = ",";

    public void writeObject(ResultData object, StringBuilder output) {
        if (object.getCalculateType() != 0) {
            output.append("ct").append("=").append(object.getCalculateType()).append(",");
        }
        if ((object.getMetricUniqueKey() != null) && (!object.getMetricUniqueKey().equals(""))) {
            output.append("muk").append("=").append(object.getMetricUniqueKey()).append(",");
        }
        long duration = object.getEndTime() - object.getStartTime();
        if (duration > 0L) {
            output.append("d").append("=").append(duration).append(",");
        }
        if (object.getCount() != 0L) {
            output.append("c").append("=").append(object.getCount()).append(",");
        }
        if (object.getCountRate() != 0.0D) {
            output.append("cr").append("=").append(CalculatorUtils.format(object.getCountRate())).append(",");
        }
        if (object.getAvgTime() != 0.0D) {
            output.append("a").append("=").append(CalculatorUtils.format(object.getAvgTime())).append(",");
        }
        if (object.getGauge() != 0L) {
            output.append("g").append("=").append(object.getGauge()).append(",");
        }
        if (object.getGaugeDouble() != 0.0D) {
            output.append("gd").append("=").append(CalculatorUtils.format(object.getGaugeDouble())).append(",");
        }
        output.append("s").append("=").append(object.getStartTime());
    }

    public ResultData readObject(String data) {
        if ((data != null) && (!data.isEmpty())) {
            String[] kvs = data.split(",");

            ResultData resultData = new ResultData();

            String[] d = null;
            for (String kv : kvs) {
                String[] kvStr = kv.split("=");
                if (kvStr.length > 1) {
                    if ("ct".equals(kvStr[0])) {
                        resultData.setCalculateType(Integer.valueOf(kvStr[1]).intValue());
                    } else if ("muk".equals(kvStr[0])) {
                        resultData.setMetricUniqueKey(kvStr[1]);
                        decodeKey(resultData, kvStr[1]);
                    } else if ("d".equals(kvStr[0])) {
                        d = kvStr;
                    } else if ("c".equals(kvStr[0])) {
                        resultData.setCount(Long.valueOf(kvStr[1]).longValue());
                    } else if ("cr".equals(kvStr[0])) {
                        resultData.setCountRate(Double.valueOf(kvStr[1]).doubleValue());
                    } else if ("a".equals(kvStr[0])) {
                        resultData.setAvgTime(Double.valueOf(kvStr[1]).doubleValue());
                    } else if ("g".equals(kvStr[0])) {
                        resultData.setGauge(Long.valueOf(kvStr[1]).longValue());
                    } else if ("gd".equals(kvStr[0])) {
                        resultData.setGaugeDouble(Double.valueOf(kvStr[1]).doubleValue());
                    } else if ("s".equals(kvStr[0])) {
                        resultData.setStartTime(Long.valueOf(kvStr[1]).longValue());
                    }
                }
            }
            resultData.setEndTime(Long.valueOf(d[1]).longValue() + resultData.getStartTime());
            return resultData;
        }
        return null;
    }


    @SuppressWarnings("unchecked")
    protected void decodeKey(ResultData object, String key) {
        if ((key != null) && (!key.isEmpty())) {
            String[] tokens = key.split("\t");
            if ((tokens != null) && (tokens.length > 0)) {
                Map<String, String> tags = null;
                for (int i = 0; i < tokens.length; i++) {
                    switch (i) {
                        case 0:
                            object.setNamespace(tokens[i]);
                            break;
                        case 1:
                            object.setMetricName(tokens[i]);
                            break;
                        default:
                            String kv = tokens[i];
                            if (!kv.isEmpty()) {
                                if (tags == null) {
                                    tags = new HashMap(tokens.length - 2);
                                }
                                String[] kvTokens = kv.split(":");
                                if ((kvTokens != null) && (kvTokens.length > 1)) {
                                    tags.put(kvTokens[0], kvTokens[1]);
                                }
                            }
                            break;
                    }
                }
                if ((tags != null) && (!tags.isEmpty())) {
                    object.setTags(tags);
                }
            }
        }
    }
}
