package com.example.order;

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
            try {
                doMysql(uuid);
            }catch (Exception ex) {
                sendRollback.send(uuid, "googs");
            }
    }
}
