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
package com.example.goods;

import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class DoReceiver {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    DoReceiver doReceiver;

    @Transactional
    public boolean doMysql(String uuid){

        List<Object> objects = jdbcTemplate.query("select * from goods where `uuid`='" + uuid + "'", new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Object();
            }
        });

        if(!CollectionUtils.isEmpty(objects)){
            return true;
        }


        //order
        int update = jdbcTemplate.update("insert into goods(uuid) values(?)",uuid);


        //order_unique
        jdbcTemplate.update("insert into goods_unique(`uuid`) values(?)",uuid);
        return true;
    }

    @Autowired
    SendRollback sendRollback;

    public void doReceiver(String uuid) throws Exception{
        //application.send(uuid);
            try {
                doMysql(uuid);
            }catch (Exception ex) {
                sendRollback.send(uuid, "googs");
            }
    }
}
