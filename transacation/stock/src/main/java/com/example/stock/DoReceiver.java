package com.example.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class DoReceiver {

    @Autowired
    StockApplication application;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void doReceiver(String uuid) throws Exception{
        List<Object> objects = jdbcTemplate.query("select * from stock_unique where `uuid`='" + uuid + "'", new RowMapper<Object>() {
            @Override
            public Object mapRow(ResultSet resultSet, int i) throws SQLException {
                return new Object();
            }
        });
        if(!CollectionUtils.isEmpty(objects)){
            //已经处理过了
            return;
        }

        /**
         * 发出货消息
         */
        application.send(uuid);
    }
}
