package com.hsh.backend.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hsh.backend.mapper.UserMapper;
import com.hsh.backend.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {


    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserMapper userMapper;
    private List<Long> list = Arrays.asList(1L, 2L);

    public PreCacheJob(RedissonClient redissonClient, RedisTemplate<String, Object> redisTemplate, UserMapper userMapper) {
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
        this.userMapper = userMapper;
    }

    @Scheduled(cron = "59 59 23 * * * ")
    public void doCacheJob() {
        RLock rLock = redissonClient.getLock("yupi:yupao:precache:lock");
        try {
            if (rLock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                log.info("getLock: {}", Thread.currentThread().getId());
                for (Long userId : list) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userMapper.selectPage(new Page<>(1, 20), queryWrapper);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    try {
                        valueOperations.set(String.format("yupi:yupao:precache:%s",userId), userPage, 1, TimeUnit.DAYS);
                    } catch (Exception e) {
                        log.error("redis set error");
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheJob Error");
            throw new RuntimeException(e);
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                log.info("unlock: {}", Thread.currentThread().getId());
                rLock.unlock();
            }
        }
    }
}
