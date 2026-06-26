package com.trung.datacrawler.service;

public class ChapterDTO {
    private final int chapterId;
    private final String title;
    private final String content;

    public ChapterDTO(int chapterId, String title, String content) {
        this.chapterId = chapterId;
        this.title = title;
        this.content = content;
    }

    public int getChapterId() { return chapterId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}