package net.gangelov.bookworm;

import net.gangelov.bookworm.readers.EPUBReader;
import net.gangelov.bookworm.words.FrequencyExtractor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class Book {
    private final String genre, title;
    private final Map<String, Integer> wordCounts;

    public Book(IBookReader reader) throws Exception {
        this.genre = null;

        title = reader.getTitle();
        wordCounts = FrequencyExtractor.extractFrom(reader.getString());
    }
    public Book(IBookReader reader, String genre) throws Exception {
        this.genre = genre;

        title = reader.getTitle();
        wordCounts = FrequencyExtractor.extractFrom(reader.getString());
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

    public static Book fromEPUB(String path, String genre) throws Exception {
        EPUBReader reader = new EPUBReader(new BufferedInputStream(new FileInputStream(path)));

        return new Book(reader, genre);
    }

    public static Book fromEPUB(String path) throws Exception {
        return fromEPUB(path, null);
    }
}
