package com.example.order;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
public class OrderApplication {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	OrderApplication application;

	private String nameServer = "localhost:9876";
	private String producerGroup ="producer";
	private String topic = "stock";


	private TransactionMQProducer producer = null;



	@Transactional
	public void doMysql(String uuid) throws Exception{
		//order
		jdbcTemplate.update("insert into `order`(uuid) values(?)",uuid);

		//order_unique
		jdbcTemplate.update("insert into order_unique(`uuid`) values(?)",uuid);
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
				//mysql

				try {
					String uuid = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);

					doMysql(uuid);

					return LocalTransactionState .COMMIT_MESSAGE;
				}catch (Exception ex){
					ex.printStackTrace();
					return LocalTransactionState.ROLLBACK_MESSAGE;
				}

			}

			@Override
			public LocalTransactionState checkLocalTransaction(MessageExt msg) {
				try {
					String uuid = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);

					jdbcTemplate.queryForObject("select * from `order` where uuid = " + uuid, new RowMapper<Object>() {
						@Override
						public Object mapRow(ResultSet resultSet, int i) throws SQLException {
							return null;
						}
					});
					return LocalTransactionState.COMMIT_MESSAGE;
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					return LocalTransactionState.ROLLBACK_MESSAGE;
				}catch (EmptyResultDataAccessException ex){
					return LocalTransactionState.ROLLBACK_MESSAGE;
				}
			}
		});

		// Specify name server addresses.
		producer.setNamesrvAddr(nameServer);
		//Launch the instance.
		producer.start();
	}


	public static void main(String[] args) {
		SpringApplication.run(OrderApplication.class, args);
	}

	@GetMapping("/start")
	public String start(){
		try {
			//发消息 库存
		    String body = UUID.randomUUID().toString();
		    Message message = new Message(topic, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
		    message.setKeys(body);
			TransactionSendResult result = producer.sendMessageInTransaction(message,new HashMap<>());
			System.out.println("库存消息" + body + "发出，结果是 + " + result);
		} catch (MQClientException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		//mysql 事务
		//确认消息 库存
		return "OK";
	}

}
