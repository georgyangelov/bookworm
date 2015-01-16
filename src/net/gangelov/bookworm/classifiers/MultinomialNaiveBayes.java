package net.gangelov.bookworm.classifiers;

import net.gangelov.bookworm.Book;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MultinomialNaiveBayes {
    public final List<String> genres;

    // {genre: number of books}
    public final Map<String, Integer> genreBookCount = new HashMap<>();

    public int totalBookCount;

    // {genre: {word: sum of tfidf}}
    public final Map<String, Map<String, Double>> genreWordCounts = new HashMap<>();

    // {word: number of books containing it}
    public final Map<String, Integer> wordBookCounts;

    public MultinomialNaiveBayes(List<String> genres, Map<String, Integer> wordBookCounts, int totalBookCount) {
        this.genres = genres;
        this.wordBookCounts = wordBookCounts;
        this.totalBookCount = totalBookCount;
    }

    public void train(Book book, String genre) throws Exception {
        // Update genreBookCount
        genreBookCount.merge(genre, 1, (a, b) -> a + b);

        Map<String, Integer> wordCounts = book.wordCounts();
        int totalWordsInBook = wordCounts.entrySet().stream().collect(Collectors.summingInt(e -> e.getValue()));
        Map<String, Double> tfidfWordCounts = wordCounts.entrySet().stream()
                .collect(
                        Collectors.toMap(
                                entry -> entry.getKey(),
                                entry -> tfidf(entry.getKey(), entry.getValue(), totalWordsInBook)
                        )
                );

        genreWordCounts.merge(
                genre,
                tfidfWordCounts,
                MultinomialNaiveBayes::mergeMaps
        );
    }

    private double tfidf(String word, int count, int totalWordsInBook) {
        double tfidf = (double)count / totalWordsInBook;

        tfidf *= Math.log((double)totalBookCount / wordBookCounts.get(word));

        return tfidf;
    }

    private static Map<String, Double> mergeMaps(Map<String, Double> map1, final Map<String, Double> map2) {
        map2.forEach((word, tfidf) -> {
            map1.merge(word, tfidf, (a, b) -> a + b);
        });

        return map1;
    }

//    public Map<String, Double> classify(Book book) {
//
//    }
}
