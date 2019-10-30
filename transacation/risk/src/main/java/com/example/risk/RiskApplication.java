package com.example.risk;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;

/**
 * 风控子系统
 */
@SpringBootApplication
@RestController
public class RiskApplication {

    private String nameServer = "localhost:9876";
    private String producerGroup ="producer";
    private String topic = "order";

    private TransactionMQProducer producer = null;

    public static void main(String[] args) {
        SpringApplication.run(RiskApplication.class,args);
    }


    @PostConstruct
    public void init() throws MQClientException {
        producer = new
                TransactionMQProducer(producerGroup);

        producer.setExecutorService(Executors.newCachedThreadPool());
        producer.setRetryTimesWhenSendAsyncFailed(10);
        producer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
                return LocalTransactionState .COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt msg) {
                return LocalTransactionState .COMMIT_MESSAGE;
            }
        });

        // Specify name server addresses.
        producer.setNamesrvAddr(nameServer);
        //Launch the instance.
        producer.start();
    }



    @GetMapping("/start")
    public String start(){
        try {
            //发消息 下单
            String body = UUID.randomUUID().toString();
            Message message = new Message(topic, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
            message.setKeys(body);
            TransactionSendResult result = producer.sendMessageInTransaction(message,new HashMap<>());
            System.out.println("下单消息" + body + "发出，结果是 + " + result);
        } catch (MQClientException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "OK";
    }
}
