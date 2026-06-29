package com.trung.datacrawler.runner;

import com.trung.datacrawler.service.ChapterDTO;
import com.trung.datacrawler.service.CrawlerService;
import com.trung.datacrawler.service.FileWriterService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class CrawlerRunner implements CommandLineRunner {

    private static final Logger log = LogManager.getLogger(CrawlerRunner.class);
    private final CrawlerService crawlerService;
    private final FileWriterService fileWriterService;

    public CrawlerRunner(CrawlerService crawlerService, FileWriterService fileWriterService) {
        this.crawlerService = crawlerService;
        this.fileWriterService = fileWriterService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== BẮT ĐẦU CÀO DỮ LIỆU BẰNG JAVA NATIVE EXECUTOR ===");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                1,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100)
        );

        try {
            runBenchmarkRound(1, executor);
            runBenchmarkRound(10, executor);
        } finally {
            executor.shutdown();
            log.info("Đã gửi lệnh Shutdown cho Executor.");

            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                log.warn("Đã ép buộc đóng các luồng do quá thời gian chờ!");
            }
        }

        log.info("=== KẾT THÚC HOÀN TOÀN ===");
    }

    private void runBenchmarkRound(int threadCount, ThreadPoolExecutor executor) throws Exception {
        Files.deleteIfExists(Paths.get("truyen_full.docx"));
        int totalChapters = 20;

        int currentMax = executor.getMaximumPoolSize();
        if (threadCount > currentMax) {
            executor.setMaximumPoolSize(threadCount);
            executor.setCorePoolSize(threadCount);
        } else {
            executor.setCorePoolSize(threadCount);
            executor.setMaximumPoolSize(threadCount);
        }

        log.info("--- ĐANG TEST VỚI CẤU HÌNH: {} LUỒNG ---", threadCount);
        long startTime = System.currentTimeMillis();

        List<CompletableFuture<ChapterDTO>> futures = new ArrayList<>();
        for (int i = 1; i <= totalChapters; i++) {
            final int chapterId = i;
            CompletableFuture<ChapterDTO> future = CompletableFuture.supplyAsync(
                    () -> crawlerService.downloadChapter(chapterId),
                    executor
            );
            futures.add(future);
        }

        List<ChapterDTO> sortedChapters = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(ChapterDTO::getChapterId))
                .collect(Collectors.toList());

        log.info("Bắt đầu nối các chương và ghi vào file DOCX...");
        for (ChapterDTO chapter : sortedChapters) {
            fileWriterService.writeChapter(chapter.getTitle(), chapter.getContent());
        }

        long endTime = System.currentTimeMillis();
        log.info(">>> KẾT QUẢ ({} LUỒNG): Hoàn thành {} chương trong {} ms\n",
                threadCount, sortedChapters.size(), (endTime - startTime));

        Thread.sleep(3000);
    }
}
