package net.gangelov.bookworm.readers;

import net.gangelov.bookworm.IBookReader;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.InputStreamReader;

class StopParsing extends SAXParseException {
    public StopParsing(String message, Locator locator) {
        super(message, locator);
    }
}

public class FB2Reader extends DefaultHandler implements IBookReader {
    private final InputStream xmlStream;
    private StringBuilder contentBuffer;

    public FB2Reader(InputStream xmlStream) {
        this.xmlStream = xmlStream;
    }

    @Override
    public String getString() throws Exception {
        InputStreamReader reader = new InputStreamReader(xmlStream, "UTF-8");
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

        InputSource inputSource = new InputSource(reader);
        inputSource.setEncoding("UTF-8");

        contentBuffer = new StringBuilder();
        try {
            parser.parse(inputSource, this);
        } catch (StopParsing error) {
            // Ignore this kind of error. It means we broke early from parsing the entire book.
        }

        return contentBuffer.toString();
    }

    private boolean inBody = false;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("body")) {
            inBody = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("body")) {
            inBody = false;
            throw new StopParsing("End of content", null);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inBody) {
            contentBuffer.append(ch, start, length);
        }
    }
}
