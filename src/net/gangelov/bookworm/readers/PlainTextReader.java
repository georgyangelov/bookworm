package net.gangelov.bookworm.readers;

import net.gangelov.bookworm.IBookReader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class PlainTextReader implements IBookReader {
    private final String title;
    private final InputStream stream;

    public PlainTextReader(String title, InputStream stream) {
        this.title = title;
        this.stream = stream;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getString() throws IOException {
        return IOUtils.toString(stream, "UTF-8");
    }
}
