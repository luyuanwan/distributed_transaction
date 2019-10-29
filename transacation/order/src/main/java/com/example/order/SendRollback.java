package com.example.order;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;


/**
 * 发送回滚消息
 */
@Service
public class SendRollback {

	private String nameServer = "localhost:9876";
	private String producerGroup ="tq";
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


	/**
	 * 发送回滚消息
	 *
	 * @param uuid
	 * @param stage
	 */
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
