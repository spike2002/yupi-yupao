package com.hsh.backend;
import java.util.ArrayList;

import com.hsh.backend.mapper.UserMapper;
import com.hsh.backend.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

@SpringBootTest
public class InsertUserTest {
    @Autowired
    private UserMapper userMapper;

    @Test
    void insertTest(){
        StopWatch stopWatch = new StopWatch();
        final int INSERT_NUM=10000;
        ArrayList<User> list = new ArrayList<>();
        stopWatch.start();
        for(int i=0;i<INSERT_NUM;i++){
            User user=new User();
            user.setUserName("test");
            user.setUserAccount("test");
            user.setAvatarUrl("");
            user.setGender(0);
            user.setUserPassword("123456");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("1000");
            user.setTags("");
            list.add(user);
        }
        userMapper.insert(list,100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    @Test
    void concurrentInsertUser(){

    }
}
