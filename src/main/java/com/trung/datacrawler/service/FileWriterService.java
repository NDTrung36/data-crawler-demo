package com.trung.datacrawler.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FileWriterService {

    private static final Logger log = LogManager.getLogger(FileWriterService.class);
    private static final String FILE_PATH = "truyen_full.txt";
    private final ReentrantLock lock = new ReentrantLock(true);

    // Đổi tham số từ chapterId (int) sang chapterTitle (String)
    public void writeChapter(String chapterTitle, String content) {
        lock.lock();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            // Định dạng lại header của mỗi chương cho nổi bật
            writer.write("==================================================\n");
            writer.write("        " + chapterTitle.toUpperCase() + "\n");
            writer.write("==================================================\n\n");

            // Ghi nội dung đã được giữ nguyên format xuống dòng
            writer.write(content + "\n\n\n");

            log.info("Đã chốt khóa và ghi thành công: {}", chapterTitle);
        } catch (IOException e) {
            log.error("Lỗi I/O khi ghi {}", chapterTitle, e);
        } finally {
            lock.unlock();
        }
    }
}
