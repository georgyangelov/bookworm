package net.gangelov.bookworm.words;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.*;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FrequencyExtractor {
    public Map<String, Integer> extractFrom(String text) throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        Analyzer analyzer = new ClassicAnalyzer(Version.LUCENE_4_9);
        TokenStream tokenStream = //new PorterStemFilter(
                new EnglishMinimalStemFilter(

                new EnglishPossessiveFilter(
                        Version.LUCENE_4_9,
                        new LowerCaseFilter(
                                Version.LUCENE_4_9,
                                analyzer.tokenStream("content", text)
                        )
                )

        );
        //);

        CharTermAttribute term = tokenStream.addAttribute(CharTermAttribute.class);
        //OffsetAttribute offset = tokenStream.addAttribute(OffsetAttribute.class);

        try {
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
//                System.out.println("Token: " + tokenStream.reflectAsString(false));
//                System.out.println("Token:  " + text.substring(offset.startOffset(), offset.endOffset()));
                String word = term.toString();
                counts.merge(word, 1, (a, b) -> a + b);
            }
        } finally {
            tokenStream.close();
        }

        return counts;
    }
}
