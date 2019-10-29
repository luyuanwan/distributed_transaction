package com.example.stock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class Receiver {

    private String nameServer = "localhost:9876";
    private String consumerGroup = "consumer";
    private String topic = "stock";

    DefaultMQPushConsumer defaultMQPushConsumer;

    @Autowired
    DoReceiver doReceiver;

    @PostConstruct
    public void init() throws MQClientException {

        defaultMQPushConsumer=new DefaultMQPushConsumer(consumerGroup);
        defaultMQPushConsumer.setNamesrvAddr(nameServer);
        defaultMQPushConsumer.subscribe(topic,"*");
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {

            /**
             * 接收上游下单模块发出的STOCK消息
             *
             * @param msgs
             * @param context
             * @return
             */
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt messageExt = msgs.get(0);


                try {

                    String uuid = new String(messageExt.getBody(), RemotingHelper.DEFAULT_CHARSET);

                    //System.out.println("receive:uuid " + uuid);

                    doReceiver.doReceiver(uuid);


                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {

                    int reconsumeTimes = messageExt.getReconsumeTimes();
                    if(reconsumeTimes >= 3){
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }

                    e.printStackTrace();
                }
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        defaultMQPushConsumer.start();
    }

    @Autowired
    SendRollback send;
}
