package com.robin.metrics.reportor;


import com.robin.metrics.calculate.ResultData;
import com.robin.metrics.channel.Channel;
import com.robin.metrics.encoding.TextCodec;
import com.robin.metrics.utils.ThreadLocalStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author: Robin.li
 * @Date: 2018/7/30
 **/

public class LoggerReporter extends ScheduledReporter {

    private static final Logger logger = LoggerFactory.getLogger(LoggerReporter.class);

    private static final ThreadLocalStringBuilder localBuffer = new ThreadLocalStringBuilder();

    private Logger output;

    private TextCodec codec;

    public LoggerReporter(String name, Channel channel) {
        super(name);
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("name should not be empty");
        }
        setChannel(channel);
        this.output = LoggerFactory.getLogger(name);
        this.codec = new TextCodec();
    }


    @Override
    public void report(List<ResultData> paramList) {

        StringBuilder sb = (StringBuilder) localBuffer.get();
//        if (sb == null)
//            sb = new StringBuilder();
        for (ResultData resultData : paramList) {
            try {
                this.codec.writeObject(resultData, sb);
                this.output.info(sb.toString());
            } catch (Throwable t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Fail to report data.", t);
                }
            } finally {
                sb.setLength(0);
            }
        }// end for

    }
}
