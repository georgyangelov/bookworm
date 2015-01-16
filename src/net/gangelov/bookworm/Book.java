package net.gangelov.bookworm;

import net.gangelov.bookworm.readers.EPUBReader;
import net.gangelov.bookworm.words.FrequencyExtractor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class Book {
    private final IBookReader reader;

    public Book(IBookReader reader) {
        this.reader = reader;
    }

    public IBookReader getReader() {
        return reader;
    }

    public String getTitle() {
        return reader.getTitle();
    }

    public Map<String, Integer> wordCounts() throws Exception {
        return FrequencyExtractor.extractFrom(getReader().getString());
    }

    public static Book fromEPUB(String path) throws IOException {
        EPUBReader reader = new EPUBReader(new BufferedInputStream(new FileInputStream(path)));

        return new Book(reader);
    }
}
