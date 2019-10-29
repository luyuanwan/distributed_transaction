package com.example.goods;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class Receiver {


    private String nameServer = "localhost:9876";
    private String consumerGroup ="c";
    private String topic = "goods";

    DefaultMQPushConsumer defaultMQPushConsumer;

    @Autowired
    DoReceiver doReceiver;

    @PostConstruct
    public void init() throws MQClientException {
        defaultMQPushConsumer=new DefaultMQPushConsumer(consumerGroup);
        defaultMQPushConsumer.setNamesrvAddr(nameServer);
        defaultMQPushConsumer.subscribe(topic,"*");
        defaultMQPushConsumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                MessageExt messageExt = msgs.get(0);

                try {
                    String uuid = new String(messageExt.getBody(), RemotingHelper.DEFAULT_CHARSET);
                    doReceiver.doReceiver(uuid);
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }
        });
        defaultMQPushConsumer.start();
    }
}
