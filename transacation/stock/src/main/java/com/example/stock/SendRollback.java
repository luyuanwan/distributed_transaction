/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.stock;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.client.producer.TransactionSendResult;
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
	private String producerGroup ="jo22";
	private String topic = "rollback";


	private TransactionMQProducer producer = null;

	@PostConstruct
	public void init() throws MQClientException {
		producer = new
				TransactionMQProducer(producerGroup);
		producer.setRetryTimesWhenSendAsyncFailed(5);
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



	public void send(String uuid,String stock){
		try {
		    String body = uuid + "," + stock;
		    Message message = new Message(topic, body.getBytes(RemotingHelper.DEFAULT_CHARSET));
			TransactionSendResult  result = producer.sendMessageInTransaction(message,new HashMap<>());

			System.out.println("回滚消息" + uuid + "发出,结果是 + " + result);


		} catch (MQClientException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
