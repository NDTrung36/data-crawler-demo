package com.trung.datacrawler.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

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

            String chapterTitle = doc.select(".chapter-title").text();
            if (chapterTitle.isEmpty()) {
                chapterTitle = "Chương " + chapterId;
            }

            Element contentElement = doc.getElementById("chapter-c");

            if (contentElement != null) {
                String htmlContent = contentElement.html();

                String formattedContent = htmlContent
                        .replaceAll("(?i)<br\\s*/?>", "\n")
                        .replaceAll("(?i)</p>", "\n\n")
                        .replaceAll("<[^>]+>", "")
                        .replace("&nbsp;", " ")
                        .trim();

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
