package com.trung.datacrawler.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class FileWriterService {

    private static final Logger log = LogManager.getLogger(FileWriterService.class);
    // Đổi đuôi file thành .docx
    private static final String FILE_PATH = "truyen_full.docx";
    private final ReentrantLock lock = new ReentrantLock(true);

    public void writeChapter(String chapterTitle, String content) {
        lock.lock();
        try {
            XWPFDocument document;
            File file = new File(FILE_PATH);

            // Kiểm tra: Nếu file đã tồn tại thì mở lên để ghi tiếp, nếu chưa thì tạo file Word mới
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    document = new XWPFDocument(fis);
                }
            } else {
                document = new XWPFDocument();
            }

            // --- 1. Ghi Tiêu đề chương ---
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER); // Căn giữa tiêu đề

            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(chapterTitle.toUpperCase());
            titleRun.setBold(true); // In đậm
            titleRun.setFontSize(16); // Cỡ chữ 16
            titleRun.addBreak(); // Xuống dòng

            // --- 2. Ghi Nội dung ---
            // Tách nội dung thành các mảng dựa trên ký tự xuống dòng
            String[] paragraphs = content.split("\n");
            for (String paraText : paragraphs) {
                if (!paraText.trim().isEmpty()) {
                    XWPFParagraph contentParagraph = document.createParagraph();
                    contentParagraph.setAlignment(ParagraphAlignment.BOTH); // Căn đều 2 bên

                    XWPFRun contentRun = contentParagraph.createRun();
                    contentRun.setText(paraText.trim());
                    contentRun.setFontSize(13); // Cỡ chữ đọc truyện chuẩn
                }
            }

            // Thêm một trang trống (Page Break) sau mỗi chương để sang chương mới sẽ nằm ở trang mới
            XWPFParagraph pageBreakParagraph = document.createParagraph();
            pageBreakParagraph.createRun().addBreak(org.apache.poi.xwpf.usermodel.BreakType.PAGE);

            // --- 3. Lưu lại file ---
            try (FileOutputStream fos = new FileOutputStream(file)) {
                document.write(fos);
            }
            document.close();

            log.info("Đã chốt khóa và ghi thành công vào file DOCX: {}", chapterTitle);

        } catch (IOException e) {
            log.error("Lỗi I/O khi ghi {}", chapterTitle, e);
        } finally {
            lock.unlock();
        }
    }
}
