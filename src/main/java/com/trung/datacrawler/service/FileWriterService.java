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

    // Khởi tạo khóa. true = Fair Lock (luồng nào đến trước được cấp khóa trước)
    private final ReentrantLock lock = new ReentrantLock(true);

    public void writeChapter(int chapterId, String content) {
        lock.lock(); // Khóa lại (Critical Section bắt đầu)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write("=== CHƯƠNG " + chapterId + " ===\n");
            writer.write(content + "\n\n");
            log.info("Đã chốt khóa và ghi thành công Chương {}", chapterId);
        } catch (IOException e) {
            log.error("Lỗi I/O khi ghi Chương {}", chapterId, e);
        } finally {
            lock.unlock(); // Luôn luôn mở khóa trong finally để tránh Deadlock nếu có lỗi
        }
    }
}
