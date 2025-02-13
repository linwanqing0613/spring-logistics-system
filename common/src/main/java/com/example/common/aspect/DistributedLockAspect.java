package com.example.common.aspect;

import com.example.common.annotation.DistributedLock;
import com.example.common.exception.BadRequestException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DistributedLockAspect {
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = distributedLock.key();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(distributedLock.waitTime(), distributedLock.expireTime(), TimeUnit.MILLISECONDS)) {
                return joinPoint.proceed();
            } else {
                throw new BadRequestException("Unable to acquire lock, try again later.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Error while acquiring lock for Object.");
        } finally {
            lock.unlock();
        }
    }
}
