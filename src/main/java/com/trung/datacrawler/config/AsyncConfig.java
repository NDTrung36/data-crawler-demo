package com.trung.datacrawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync // Bắt buộc phải có để Spring kích hoạt tính năng chạy bất đồng bộ (@Async)
public class AsyncConfig {

    @Bean(name = "crawlerTaskExecutor")
    public Executor crawlerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Số lượng luồng luôn luôn "thường trực" trong hệ thống (Tối thiểu 5 công nhân)
        executor.setCorePoolSize(5);

        // Số lượng luồng tối đa hệ thống có thể nở ra khi hàng đợi bị đầy
        executor.setMaxPoolSize(10);

        // Sức chứa của hàng đợi. Nếu có 50 chương truyện đổ về, 5 luồng xử lý,
        // 45 chương còn lại sẽ nằm chờ trong hàng đợi này.
        executor.setQueueCapacity(100);

        // Đặt tên tiền tố cho các luồng để khi log ra console ta dễ nhận biết
        executor.setThreadNamePrefix("NovelThread-");

        executor.initialize();
        return executor;
    }
}
