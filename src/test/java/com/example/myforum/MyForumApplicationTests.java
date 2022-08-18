package com.example.myforum;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest(classes = MyForumApplicationTests.class)
@EnableAutoConfiguration
class MyForumApplicationTests {

    @Autowired
    DruidDataSource druidDataSource;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void contextLoads() throws SQLException {
        Connection connection = druidDataSource.getConnection();
        System.out.println(connection);

        System.out.println(sqlSessionFactory.openSession().getConnection());
    }

}
