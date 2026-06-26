package com.trung.datacrawler.runner;

import com.trung.datacrawler.service.CrawlerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
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

    public CrawlerRunner(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("--- BẮT ĐẦU HỆ THỐNG CÀO TRUYỆN ĐA LUỒNG ---");

        // Xóa file cũ nếu có để tránh ghi nối vào kết quả của lần chạy trước
        Files.deleteIfExists(Paths.get("truyen_full.txt"));

        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 1; i <= 50; i++) {
            CompletableFuture<Void> future = crawlerService.downloadChapter(i);
            futures.add(future);
        }

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join();

        long endTime = System.currentTimeMillis();

        log.info("--- TẤT CẢ CÁC LUỒNG ĐÃ HOÀN THÀNH ---");
        log.info("Tổng thời gian tải và ghi 50 chương là: {} ms", (endTime - startTime));
        log.info("Hãy mở file truyen_full.txt ở thư mục gốc project để kiểm tra.");
    }
}