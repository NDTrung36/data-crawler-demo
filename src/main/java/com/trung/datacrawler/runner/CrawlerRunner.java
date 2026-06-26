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
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class CrawlerRunner implements CommandLineRunner {

    private static final Logger log = LogManager.getLogger(CrawlerRunner.class);
    private final CrawlerService crawlerService;
    private final FileWriterService fileWriterService;

    // XÓA inject ThreadPoolTaskExecutor của Spring

    public CrawlerRunner(CrawlerService crawlerService, FileWriterService fileWriterService) {
        this.crawlerService = crawlerService;
        this.fileWriterService = fileWriterService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== BẮT ĐẦU CÀO DỮ LIỆU BẰNG JAVA NATIVE EXECUTOR ===");

        // 1. TỰ KHỞI TẠO THREAD POOL BẰNG JAVA NATIVE
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,                      // Core Pool Size (Sẽ thay đổi lúc Benchmark)
                1,                      // Max Pool Size
                60L, TimeUnit.SECONDS,  // Thời gian luồng rảnh rỗi trước khi bị hủy
                new LinkedBlockingQueue<>(100) // Hàng đợi chứa 100 nhiệm vụ
        );

        try {
            // Chạy 3 vòng đua
            runBenchmarkRound(1, executor);
            runBenchmarkRound(10, executor);

        } finally {
            // 2. CỰC KỲ QUAN TRỌNG: Phải tự đóng Executor, nếu không app sẽ treo mãi mãi
            executor.shutdown();
            log.info("Đã gửi lệnh Shutdown cho Executor.");

            // Chờ tối đa 1 phút để các luồng đang dở tay hoàn thành nốt
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Ép buộc giết các luồng nếu quá hạn
                log.warn("Đã ép buộc đóng các luồng do quá thời gian chờ!");
            }
        }

        log.info("=== KẾT THÚC HOÀN TOÀN ===");
    }

    private void runBenchmarkRound(int threadCount, ThreadPoolExecutor executor) throws Exception {
        Files.deleteIfExists(Paths.get("truyen_full.docx"));
        int totalChapters = 20;

        // Điều chỉnh số luồng của Java Native Executor
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

        // Cấp phát công việc
        List<CompletableFuture<ChapterDTO>> futures = new ArrayList<>();
        for (int i = 1; i <= totalChapters; i++) {
            final int chapterId = i; // Bắt buộc phải gán biến final/effective final để dùng trong Lambda

            // Dùng supplyAsync và TRUYỀN EXECUTOR CỦA CHÚNG TA VÀO.
            // Nếu không truyền, Java sẽ tự dùng ForkJoinPool mặc định.
            CompletableFuture<ChapterDTO> future = CompletableFuture.supplyAsync(
                    () -> crawlerService.downloadChapter(chapterId),
                    executor
            );
            futures.add(future);
        }

        // Chờ kết quả, bóc tách và sắp xếp
        List<ChapterDTO> sortedChapters = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(ChapterDTO::getChapterId))
                .collect(Collectors.toList());

        // Ghi tuần tự ra file
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