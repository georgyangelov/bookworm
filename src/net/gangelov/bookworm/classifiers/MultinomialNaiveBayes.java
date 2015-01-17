package net.gangelov.bookworm.classifiers;

import net.gangelov.bookworm.Book;
import net.gangelov.bookworm.BookTrainSet;
import net.gangelov.bookworm.words.TfIdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MultinomialNaiveBayes {
    // {genre: {word: sum of tfidf}}
    public Map<String, Map<String, Double>> genreWordCounts;

    private final BookTrainSet trainSet;
    private final TfIdf tfidf;

    public MultinomialNaiveBayes(BookTrainSet trainSet) {
        this.trainSet = trainSet;
        this.tfidf = new TfIdf(trainSet);

        train();
    }

    public void train() {
        genreWordCounts = trainSet.getGenres().stream()
            .collect(Collectors.toMap(
                    genre -> genre,
                    genre -> wordCountsForGenre(genre)
            ));
    }

    private Map<String, Double> wordCountsForGenre(String genre) {
         return trainSet.booksForGenre(genre).parallelStream()
                .map(tfidf::calculate)
                .flatMap(wordCounts -> wordCounts.entrySet().parallelStream())
                .collect(Collectors.toConcurrentMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a + b
                ));
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
