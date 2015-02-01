package net.gangelov.bookworm.words.filters;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.regex.Pattern;

public class InvalidWordsFilter extends TokenFilter {
    private CharTermAttribute term;

    private static final Pattern VALID_WORD_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я]+$");
    private static final Pattern VALID_NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

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

            if (isValidNumber(word)) {
                changeNumber(word);

                return true;
            } else if (isValidWord(word)) {
                return true;
            }
        }
    }

    private boolean isValidWord(String word) {
        return VALID_WORD_PATTERN.matcher(word).matches();
    }

    private boolean isValidNumber(String word) {
        return VALID_NUMBER_PATTERN.matcher(word).matches();
    }

    private void changeNumber(String word) {
        long number;
        try {
             number = Long.parseLong(word);
        } catch (NumberFormatException e) {
            changeWord("extremely-large-number");
            return;
        }

        if (number < 10) {
            changeWord("very-small-number");
        } else if (number < 100) {
            changeWord("small-number");
        } else if (number < 1000) {
            changeWord("large-number");
        } else if (number < 3000) {
            changeWord("year-number");
        } else {
            changeWord("very-large-number");
        }
    }

    private void changeWord(String newWord) {
        term.setEmpty();
        term.append(newWord);
    }
}
