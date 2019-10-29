package com.example.rollback;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DoReceiver {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    DoReceiver doReceiver;



    @Transactional
    public void doMysql(String uuid,String stage) throws Exception{

        System.out.println(stage + "---------------------" + uuid);//order

            try {
                jdbcTemplate.update("insert into rollback_unique(`uuid`) values(?)",uuid);
            }catch (Exception ex){
                return ;
            }


            jdbcTemplate.update("delete from `order` where uuid='" + uuid + "'");
            jdbcTemplate.update("delete from `goods` where uuid='" + uuid + "'");
            if(!"stock".equals(stage)) {
                jdbcTemplate.update("update stock set stock=stock+1 where id=1");
            }


    }

    public void doReceiver(String uuid,String stage) throws Exception{
        //application.send(uuid);
        doMysql(uuid,stage);
    }
}
