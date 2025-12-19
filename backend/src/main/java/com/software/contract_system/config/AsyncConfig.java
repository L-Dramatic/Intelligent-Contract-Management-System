package com.software.contract_system.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 
 * 用于配置异步任务执行器，主要用于：
 * 1. AI审查的异步执行
 * 2. 其他耗时操作的异步处理
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 配置异步任务执行器
     * 
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("初始化异步任务执行器...");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：保持活跃的线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数：允许的最大线程数
        executor.setMaxPoolSize(20);
        
        // 队列容量：等待队列的大小
        executor.setQueueCapacity(100);
        
        // 线程名前缀
        executor.setThreadNamePrefix("Async-Review-");
        
        // 拒绝策略：当线程池和队列都满时的处理策略
        // CallerRunsPolicy: 由调用线程处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待终止的时间
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化
        executor.initialize();
        
        log.info("异步任务执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        
        return executor;
    }
}

