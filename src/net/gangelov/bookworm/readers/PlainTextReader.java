package net.gangelov.bookworm.readers;

import net.gangelov.bookworm.IBookReader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class PlainTextReader implements IBookReader {
    private final InputStream stream;

    public PlainTextReader(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public String getString() throws IOException {
        return IOUtils.toString(stream, "UTF-8");
    }
}
