package net.gangelov.bookworm.words;

import net.gangelov.bookworm.words.filters.InvalidWordsFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class FrequencyExtractor {
    public static Map<String, Integer> extractFrom(String text) throws IOException {
        Map<String, Integer> counts = new HashMap<>();
//        Analyzer analyzer = new ClassicAnalyzer(Version.LUCENE_4_9);
        Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_4_9, new StringReader(text));
        TokenStream tokenStream =
//new PorterStemFilter(
                new InvalidWordsFilter(
//                new EnglishMinimalStemFilter(
//                    new EnglishPossessiveFilter(Version.LUCENE_4_9,
//                            new StopFilter(Version.LUCENE_4_9,
                                new LowerCaseFilter(Version.LUCENE_4_9,
                                        tokenizer
                                )//,
//                                StopAnalyzer.ENGLISH_STOP_WORDS_SET
//                            )
//                    )
//                )
                //);
        );

        CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
        //OffsetAttribute offset = tokenStream.addAttribute(OffsetAttribute.class);

        try {
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
//                System.out.println("Token: " + tokenStream.reflectAsString(false));
//                System.out.println("Token:  " + text.substring(offset.startOffset(), offset.endOffset()));
                String word = term.toString();
                counts.merge(word, 1,
                        (a, b) -> a + b);
            }
        } finally {
            tokenStream.close();
        }

        return counts;
    }
}
