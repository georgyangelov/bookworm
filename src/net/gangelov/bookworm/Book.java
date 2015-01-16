package net.gangelov.bookworm;

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
}
