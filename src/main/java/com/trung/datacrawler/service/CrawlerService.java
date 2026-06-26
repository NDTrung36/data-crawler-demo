package com.trung.datacrawler.service;


import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Service
public class CrawlerService {

    // Trỏ bean "crawlerTaskExecutor" đã cấu hình ở Bước 2 vào đây
    @Async("crawlerTaskExecutor")
    public CompletableFuture<String> downloadChapter(int chapterId) {
        // Log ra để xem luồng nào đang xử lý chương nào
        System.out.println(Thread.currentThread().getName() + " bắt đầu tải chương: " + chapterId);

        try {
            // Giả lập thời gian tải ngẫu nhiên từ 500ms đến 1500ms
            int downloadTime = new Random().nextInt(1000) + 500;
            Thread.sleep(downloadTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Trả về kết quả bọc trong CompletableFuture (Lời hứa sẽ có kết quả trong tương lai)
        return CompletableFuture.completedFuture("Nội dung của chương " + chapterId);
    }
}