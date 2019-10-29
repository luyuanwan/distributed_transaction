package com.example.goods;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;


@Service
public class SendRollback {


	private String nameServer = "localhost:9876";
	private String producerGroup ="producer";
	private String topic = "rollback";


	private TransactionMQProducer producer = null;

	@PostConstruct
	public void init() throws MQClientException {
		producer = new
				TransactionMQProducer(producerGroup);
		producer.setTransactionListener(new TransactionListener() {
			@Override
			public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
				//mysql
				return LocalTransactionState.COMMIT_MESSAGE;
			}

			@Override
			public LocalTransactionState checkLocalTransaction(MessageExt msg) {
				return LocalTransactionState.COMMIT_MESSAGE;
			}
		});

		// Specify name server addresses.
		producer.setNamesrvAddr(nameServer);
		//Launch the instance.
		producer.start();
	}


	public static void main(String[] args) {
		SpringApplication.run(SendRollback.class, args);
	}

	public void send(String uuid,String stage){
		try {
		    String body = uuid + "," + stage;
		    Message message = new Message(topic, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
			producer.sendMessageInTransaction(message,new HashMap<>());
		} catch (MQClientException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
