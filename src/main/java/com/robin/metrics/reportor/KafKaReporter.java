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
 * @Date: 2018/8/14
 **/

public class KafKaReporter extends ScheduledReporter {

    private static final Logger logger = LoggerFactory.getLogger(KafKaReporter.class);

    private static final ThreadLocalStringBuilder localBuffer = new ThreadLocalStringBuilder();

    private TextCodec codec;

    //private KafkaClient kafkaClient;


    public KafKaReporter(String name, Channel channel) {
        super(name);
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("name should not be empty");
        }
        setChannel(channel);

        // kafkaClient =
        this.codec = new TextCodec();

    }

    @Override
    public void report(List<ResultData> paramList) {

        StringBuilder stringBuilder = localBuffer.get();

        for (ResultData resultData : paramList) {
            try {
                codec.writeObject(resultData,stringBuilder);
                /**
                 * Kafka 逻辑
                 */


            }catch (Exception ex){

            }finally {

            }
        }
    }
}
