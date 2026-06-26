package com.trung.datacrawler.runner;

import com.trung.datacrawler.service.CrawlerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CrawlerRunner implements CommandLineRunner {

    private static final Logger log = LogManager.getLogger(CrawlerRunner.class);
    private final CrawlerService crawlerService;
    private final ThreadPoolTaskExecutor taskExecutor; // Inject TaskExecutor

    public CrawlerRunner(CrawlerService crawlerService, ThreadPoolTaskExecutor taskExecutor) {
        this.crawlerService = crawlerService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== BẮT ĐẦU GIAI ĐOẠN 4: BENCHMARK ĐA LUỒNG ===");

        // Chạy 3 vòng đua với cấu hình luồng khác nhau
        runBenchmarkRound(1);  // Vòng 1: Đơn luồng (Tuần tự)
        runBenchmarkRound(10); // Vòng 2: 10 luồng
        runBenchmarkRound(50); // Vòng 3: 50 luồng

        log.info("=== KẾT THÚC BENCHMARK ===");
        // Spring Boot sẽ tự động thoát vì ứng dụng không có Web Server
    }

    private void runBenchmarkRound(int threadCount) throws Exception {
        Files.deleteIfExists(Paths.get("truyen_full.docx"));

        // --- ĐOẠN CODE ĐƯỢC FIX ---
        int currentMax = taskExecutor.getMaxPoolSize();
        if (threadCount > currentMax) {
            // Đang scale up: Tăng Max trước, Core sau
            taskExecutor.setMaxPoolSize(threadCount);
            taskExecutor.setCorePoolSize(threadCount);
        } else {
            // Đang scale down: Giảm Core trước, Max sau
            taskExecutor.setCorePoolSize(threadCount);
            taskExecutor.setMaxPoolSize(threadCount);
        }
        // --------------------------

        log.info("--- ĐANG TEST VỚI CẤU HÌNH: {} LUỒNG ---", threadCount);
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            CompletableFuture<Void> future = crawlerService.downloadChapter(i);
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        long endTime = System.currentTimeMillis();
        log.info(">>> KẾT QUẢ ({} LUỒNG): Tải 20 chương mất {} ms\n", threadCount, (endTime - startTime));

        Thread.sleep(3000);
    }
}