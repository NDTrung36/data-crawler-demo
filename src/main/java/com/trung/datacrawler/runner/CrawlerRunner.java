package com.trung.datacrawler.runner;


import com.trung.datacrawler.service.CrawlerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class CrawlerRunner implements CommandLineRunner {

    private final CrawlerService crawlerService;

    // Inject service vào runner
    public CrawlerRunner(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- BẮT ĐẦU HỆ THỐNG CÀO TRUYỆN ĐA LUỒNG ---");
        long startTime = System.currentTimeMillis();

        // Danh sách chứa các "Lời hứa" (Future) từ luồng con
        List<CompletableFuture<String>> futures = new ArrayList<>();

        // Giả lập phát lệnh tải đồng thời 50 chương truyện từ chương 1 đến 50
        for (int i = 1; i <= 50; i++) {
            CompletableFuture<String> future = crawlerService.downloadChapter(i);
            futures.add(future);
        }

        // Dòng lệnh QUAN TRỌNG: Ép luồng chính (Main Thread) phải đứng đợi
        // cho đến khi TẤT CẢ các luồng con (50 futures) hoàn thành xong nhiệm vụ.
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allOf.join(); // Block luồng chính tại đây cho đến khi xong hết

        long endTime = System.currentTimeMillis();
        System.out.println("--- TẤT CẢ CÁC LUỒNG ĐÃ HOÀN THÀNH ---");
        System.out.println("Tổng thời gian tải 50 chương là: " + (endTime - startTime) + " ms");

        // Thử in ra kết quả của một chương bất kỳ để kiểm tra xem dữ liệu về đúng chưa
        System.out.println("Kiểm tra dữ liệu chương 5: " + futures.get(4).get());
    }
}