package com.example.stock;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.swing.text.html.HTMLDocument;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class StockApplication {



	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	StockApplication application;

	private String nameServer = "localhost:9876";
	private String producerGroup ="producer";
	private String topic = "goods";


	private TransactionMQProducer producer = null;

	@Autowired
	SendRollback sendRollback;

	@Transactional
	public boolean doMysql(String uuid) throws Exception{

		//System.out.println("start ------------- receive uuid is " + uuid);
		//order
		int update = jdbcTemplate.update("update stock set stock=stock-1 where id = 1 and stock > 0");
		if(update <= 0){
			//System.out.println("start ------------- send rollback for uuid is " + uuid);
			sendRollback.send(uuid,"stock");
			return false;
		}

		//order_unique
		jdbcTemplate.update("insert into stock_unique(`uuid`) values(?)",uuid);

		return true;
	}

	@PostConstruct
	public void init() throws MQClientException {
		producer = new
				TransactionMQProducer(producerGroup);
		producer.setTransactionListener(new TransactionListener() {
			@Override
			public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
				//mysql

				try {
					String uuid = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);

					if(!doMysql(uuid)){
						return LocalTransactionState.ROLLBACK_MESSAGE;
					}

					return LocalTransactionState.COMMIT_MESSAGE;
				}catch (Exception ex){
					ex.printStackTrace();
					return LocalTransactionState.ROLLBACK_MESSAGE;
				}

			}

			@Override
			public LocalTransactionState checkLocalTransaction(MessageExt msg) {
				try {
					String uuid = new String(msg.getBody(), RemotingHelper.DEFAULT_CHARSET);

					jdbcTemplate.queryForObject("select * from stock_unique where `unique` = " + uuid, new RowMapper<Object>() {
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




	/**
	 * 发出货消息
	 */
	public void send(String uuid){
		try {
			String body = uuid;
			Message message = new Message(topic, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
			TransactionSendResult result = producer.sendMessageInTransaction(message,new HashMap<>());


			System.out.println("出货消息" + uuid + "发出,结果是 + " + result);
		} catch (MQClientException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(StockApplication.class, args);
	}

}
