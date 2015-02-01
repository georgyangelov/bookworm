package net.gangelov.bookworm;

import net.gangelov.bookworm.readers.EPUBReader;
import net.gangelov.bookworm.readers.FB2Reader;
import net.gangelov.bookworm.words.FrequencyExtractor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class Book {
    private final String genre, title;
    private final Map<String, Integer> wordCounts;
    private final int totalWordCount;

    public Book(IBookReader reader) throws Exception {
        this.genre = null;

        title = reader.getTitle();
        wordCounts = FrequencyExtractor.extractFrom(reader.getString());
        totalWordCount = wordCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
    public Book(IBookReader reader, String genre) throws Exception {
        this.genre = genre;

        title = reader.getTitle();
        wordCounts = FrequencyExtractor.extractFrom(reader.getString());
        totalWordCount = wordCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public Map<String, Integer> getWordCounts() {
        return wordCounts;
    }

    public int getTotalWordCount() {
        return totalWordCount;
    }

    public static Book fromEPUB(String path, String genre) throws Exception {
        EPUBReader reader = new EPUBReader(new BufferedInputStream(new FileInputStream(path)));

        return new Book(reader, genre);
    }

    public static Book fromEPUB(String path) throws Exception {
        return fromEPUB(path, null);
    }

    public static Book fromFB2(String path, String genre) throws Exception {
        FB2Reader reader = new FB2Reader(new BufferedInputStream(new FileInputStream(path)));

        return new Book(reader, genre);
    }

    public static Book fromFB2(String path) throws Exception {
        return fromFB2(path, null);
    }
}
