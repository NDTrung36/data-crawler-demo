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

    public ChapterDTO downloadChapter(int chapterId) {
        String url = String.format("https://truyenfull.today/dao-quan/chuong-%d/", chapterId);
        log.info("Đang kết nối để tải: {}", url);

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get();

            // 1. Cào tiêu đề chương (VD: "Chương 1: Không Uổng Công")
            // Truyenfull thường để title ở class .chapter-title
            String chapterTitle = doc.select(".chapter-title").text();
            if (chapterTitle.isEmpty()) {
                chapterTitle = "Chương " + chapterId; // Fallback nếu không tìm thấy
            }

            // 2. Cào và xử lý nội dung
            Element contentElement = doc.getElementById("chapter-c");

            if (contentElement != null) {
                // Lấy HTML thô thay vì text
                String htmlContent = contentElement.html();

                // Dùng Regex để biến HTML thành Text giữ nguyên format
                String formattedContent = htmlContent
                        // Thay thế thẻ <br> (hoặc <br/>, <br />) thành 1 lần xuống dòng
                        .replaceAll("(?i)<br\\s*/?>", "\n")
                        // Thay thế thẻ đóng </p> thành 2 lần xuống dòng để tạo khoảng cách đoạn
                        .replaceAll("(?i)</p>", "\n\n")
                        // Xóa sạch tất cả các thẻ HTML còn sót lại (<div>, <i>, <b>...)
                        .replaceAll("<[^>]+>", "")
                        // Chuyển ký tự khoảng trắng đặc biệt của HTML thành dấu cách bình thường
                        .replace("&nbsp;", " ")
                        // Cắt khoảng trắng thừa ở đầu và cuối bài
                        .trim();

                // Truyền title và content đã format sang Service ghi file
                fileWriterService.writeChapter(chapterTitle, formattedContent);
            } else {
                log.warn("Chương {}: Không tìm thấy thẻ HTML chứa nội dung!", chapterId);
            }

        } catch (Exception e) {
            log.error("Lỗi khi tải chương {}: {}", chapterId, e.getMessage());
        }

        return null;
    }
}