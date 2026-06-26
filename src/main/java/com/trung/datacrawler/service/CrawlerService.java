package com.trung.datacrawler.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CrawlerService {

    private static final Logger log = LogManager.getLogger(CrawlerService.class);
    private final FileWriterService fileWriterService;

    public CrawlerService(FileWriterService fileWriterService) {
        this.fileWriterService = fileWriterService;
    }

    @Async("crawlerTaskExecutor")
    public CompletableFuture<Void> downloadChapter(int chapterId) {
        // Áp dụng quy luật URL của bạn
        String url = String.format("https://truyenfull.today/dao-quan/chuong-%d/", chapterId);
        log.info("Đang kết nối để tải: {}", url);

        try {
            // Dùng Jsoup kết nối đến URL, set timeout 10 giây tránh bị treo luồng
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)") // Giả lập trình duyệt để tránh bị block
                    .timeout(10000)
                    .get();

            // Bóc tách nội dung (Phân tích HTML trang truyenfull.today thì ID chứa chữ là chapter-c)
            Element contentElement = doc.getElementById("chapter-c");

            if (contentElement != null) {
                // Lấy toàn bộ text bên trong, Jsoup tự động xử lý các thẻ <br> thành dấu xuống dòng
                String content = contentElement.text();

                // Ghi vào file thông qua khóa ReentrantLock đã làm ở Giai đoạn 3
                fileWriterService.writeChapter(chapterId, content);
            } else {
                log.warn("Chương {}: Không tìm thấy thẻ HTML chứa nội dung!", chapterId);
            }

        } catch (Exception e) {
            // Bắt lỗi nếu rớt mạng, trang web sập, hoặc timeout
            log.error("Lỗi khi tải chương {}: {}", chapterId, e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }
}