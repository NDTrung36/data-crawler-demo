package com.trung.datacrawler.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class CrawlerService {

    private static final Logger log = LogManager.getLogger(CrawlerService.class);
    private final FileWriterService fileWriterService;

    // Inject FileWriterService
    public CrawlerService(FileWriterService fileWriterService) {
        this.fileWriterService = fileWriterService;
    }

    @Async("crawlerTaskExecutor")
    public CompletableFuture<Void> downloadChapter(int chapterId) {
        log.info("Bắt đầu tải chương: {}", chapterId);

        try {
            int downloadTime = new Random().nextInt(1000) + 500;
            Thread.sleep(downloadTime);

            // Giả lập nội dung tải được
            String mockContent = "Đây là nội dung cực kỳ hấp dẫn của chương " + chapterId;

            // Tải xong thì gọi hàm ghi file ngay lập tức
            fileWriterService.writeChapter(chapterId, mockContent);

        } catch (InterruptedException e) {
            log.error("Luồng bị gián đoạn khi tải chương: {}", chapterId, e);
            Thread.currentThread().interrupt();
        }

        // Đổi kiểu trả về thành CompletableFuture<Void> vì ta đã tự ghi file, không cần gom text về Runner nữa
        return CompletableFuture.completedFuture(null);
    }
}