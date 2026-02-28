package com.hsh.backend;

import com.hsh.backend.model.entity.User;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
public class RedissonTest {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void test(){
        RList<Object> rList = redissonClient.getList("test-ridisson-list");
        User user = new User();
        user.setUserName("test");
        rList.add("test");
        rList.add(1);
        rList.add(new int[]{1,2,3});
        rList.add(user);
        System.out.println(rList.get(0));
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("test1","test");
        valueOperations.set("test2","test");
        valueOperations.set("test3","test");
        valueOperations.set("test4",user);
        HashOperations<String, Object, Object> forHash = redisTemplate.opsForHash();
        forHash.put("test5","user1",user);
        forHash.put("test5","user2",user);
        System.out.println(valueOperations.get("test1"));
    }
}
