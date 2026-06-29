package com.trung.datacrawler.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.BreakType;
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
    private static final String FILE_PATH = "truyen_full.docx";
    private final ReentrantLock lock = new ReentrantLock(true);

    public void writeChapter(String chapterTitle, String content) {
        lock.lock();
        try {
            XWPFDocument document;
            File file = new File(FILE_PATH);

            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    document = new XWPFDocument(fis);
                }
            } else {
                document = new XWPFDocument();
            }

            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText(chapterTitle.toUpperCase());
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.addBreak();

            String[] paragraphs = content.split("\n");
            for (String paraText : paragraphs) {
                if (!paraText.trim().isEmpty()) {
                    XWPFParagraph contentParagraph = document.createParagraph();
                    contentParagraph.setAlignment(ParagraphAlignment.BOTH);

                    XWPFRun contentRun = contentParagraph.createRun();
                    contentRun.setText(paraText.trim());
                    contentRun.setFontSize(13);
                }
            }

            XWPFParagraph pageBreakParagraph = document.createParagraph();
            pageBreakParagraph.createRun().addBreak(BreakType.PAGE);

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
