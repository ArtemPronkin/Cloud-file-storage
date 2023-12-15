package com.example.demo.service.s3Storage;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Aspect
public class AspectBlock {
    @Pointcut("within(com.example.demo.service.s3Storage.SyncBlockService..*)")
    public void pointcut() {
    }

    @Before("execution(public * com.example.demo.service.s3Storage.S3StorageServiceWithSync.*(..))")
    public void before(JoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        SyncBlockService.occupiedBucket((String) args[0]);
        log.info("Block Bucket : " + args[0]);
    }

    @After("execution(public * com.example.demo.service.s3Storage.S3StorageServiceWithSync.*(..))")
    public void after(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        SyncBlockService.emptyBucket((String) args[0]);
        log.info("UnBlock Bucket : " + args[0]);
    }

}
