package com.hsh.backend;

import java.util.ArrayList;
import java.util.concurrent.*;

import com.hsh.backend.mapper.UserMapper;
import com.hsh.backend.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StopWatch;

@SpringBootTest
public class InsertUserTest {
    @Autowired
    private UserMapper userMapper;

//    private ThreadPoolExecutor executor = new ThreadPoolExecutor(50, 100, 1000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000));

    private ExecutorService executorService = new ThreadPoolExecutor(20, 100, 60, TimeUnit.MINUTES, new ArrayBlockingQueue<>(100));

//    private ExecutorService executor2 = new ThreadPoolExecutor(50, 100, 1000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000));

//    private Executor executor3 = new ThreadPoolTaskExecutor();

    @Test
    void insertTest() {
        StopWatch stopWatch = new StopWatch();
        final int INSERT_NUM = 10000;
        ArrayList<User> list = new ArrayList<>();
        stopWatch.start();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
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
        userMapper.insert(list, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTime(TimeUnit.SECONDS));
    }

    @Test
    void concurrentInsertUser() {
        StopWatch stopWatch = new StopWatch();
        int batchSize = 100;
        ArrayList<CompletableFuture<Void>> futures = new ArrayList<>();
        stopWatch.start();
        for (int i = 0; i < 100; i++) {
            int j = 0;
            ArrayList<User> list = new ArrayList<>();
            while (true) {
                User user = new User();
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
                j++;
                if (j % batchSize == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                userMapper.insert(list);
                System.out.println("Running Thread is" + Thread.currentThread().getName());
            }, executorService);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTime(TimeUnit.SECONDS));
    }
}
