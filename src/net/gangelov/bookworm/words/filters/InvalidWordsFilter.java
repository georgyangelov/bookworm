package net.gangelov.bookworm.words.filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Pattern;

public class InvalidWordsFilter extends TokenFilter {
    private CharTermAttribute term;

    private static final Pattern VALID_WORD_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    public InvalidWordsFilter(TokenStream input) {
        super(input);

        term = input.addAttribute(CharTermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (true) {
            if (!input.incrementToken()) {
                return false;
            }

            String word = term.toString();

            if (isValid(word)) {
                return true;
            }
        }
    }

    private boolean isValid(String word) {
        return VALID_WORD_PATTERN.matcher(word).matches();
    }
}
