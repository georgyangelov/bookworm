package net.gangelov.bookworm.readers;

import net.gangelov.bookworm.IBookReader;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.Spine;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.service.MediatypeService;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.Normalizer;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EPUBReader implements IBookReader {
    private final InputStream fileStream;

    public EPUBReader(InputStream fileStream) {
        this.fileStream = fileStream;
    }

    @Override
    public String getString() throws Exception {
        EpubReader epubReader = new EpubReader();

        Book book = epubReader.readEpub(fileStream);
        Spine spine = book.getSpine();

        StringBuilder content = new StringBuilder();

        for (int i = 0; i < spine.size(); i++) {
            Resource resource = spine.getResource(i);

            content.append(getContent(resource));
        }

        return content.toString();
    }

    /* Shamelessly copied from epublib-tools (with modifications). */

    public static int NBSP = 0x00A0;

    // whitespace pattern that also matches U+00A0 (&nbsp; in html)
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\p{Z}\\s]+");
    private static final Pattern REMOVE_ACCENT_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private static String getContent(Resource resource) throws IOException {
        if (resource.getMediaType() != MediatypeService.XHTML) {
            return "";
        }

        return getContent(resource.getReader());
    }

    private static String getContent(Reader content) {
        StringBuilder result = new StringBuilder();
        Scanner scanner = new Scanner(content);
        scanner.useDelimiter("<");
        while (scanner.hasNext()) {
            String text = scanner.next();
            int closePos = text.indexOf('>');

            String tag = text.substring(0, closePos).split(" ")[0];
            if (tag.equalsIgnoreCase("style") || tag.equalsIgnoreCase("script") || tag.equalsIgnoreCase("title")) {
                // Ignore contents
                continue;
            }

            if (tag.equalsIgnoreCase("p")) {
                result.append(" ");
            }

            String chunk = text.substring(closePos + 1).trim();
            chunk = StringEscapeUtils.unescapeHtml(chunk);
            chunk = cleanText(chunk);
            result.append(chunk);
        }

        return result.toString();
    }

    private static boolean isHtmlWhitespace(int c) {
        return c == NBSP || Character.isWhitespace(c);
    }

    private static String unicodeTrim(String text) {
        int leadingWhitespaceCount = 0;
        int trailingWhitespaceCount = 0;
        for (int i = 0; i < text.length(); i++) {
            if (! isHtmlWhitespace(text.charAt(i))) {
                break;
            }
            leadingWhitespaceCount++;
        }
        for (int i = (text.length() - 1); i > leadingWhitespaceCount; i--) {
            if (! isHtmlWhitespace(text.charAt(i))) {
                break;
            }
            trailingWhitespaceCount++;
        }
        if (leadingWhitespaceCount > 0 || trailingWhitespaceCount > 0) {
            text = text.substring(leadingWhitespaceCount, text.length() - trailingWhitespaceCount);
        }
        return text;
    }

    private static String cleanText(String text) {
        text = unicodeTrim(text);

        // replace all multiple whitespaces by a single space
        Matcher matcher = WHITESPACE_PATTERN.matcher(text);
        text = matcher.replaceAll(" ");

        // turn accented characters into normalized form. Turns &ouml; into o"
        text = Normalizer.normalize(text, Normalizer.Form.NFD);

        // removes the marks found in the previous line.
        text = REMOVE_ACCENT_PATTERN.matcher(text).replaceAll("");

        return text;
    }
}
