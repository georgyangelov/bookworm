package net.gangelov.bookworm.classifiers;

import net.gangelov.bookworm.Book;
import net.gangelov.bookworm.BookTrainSet;
import net.gangelov.bookworm.words.TfIdf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TransformedWeightNormalizedComplementNaiveBayes {
    // {genre: {word: sum of tfidf}}
    private Map<String, Map<String, Double>> genreWordCounts;

    // {genre: sum of tfidf for all words}
    private Map<String, Double> genreTotalCounts;

    private int totalWordCount;

    private final BookTrainSet trainSet;
    private final TfIdf tfidf;

    public TransformedWeightNormalizedComplementNaiveBayes(BookTrainSet trainSet) {
        this.trainSet = trainSet;
        this.tfidf = new TfIdf(trainSet);

        train();
    }

    public String classify(Book book) {
        Map<String, Double> weights = classificationWeights(book);

        return weights.entrySet().stream().min((a, b) -> a.getValue().compareTo(b.getValue())).get().getKey();
    }

    public Map<String, Double> classificationWeights(Book book) {
        return trainSet.getGenres().stream()
                .collect(Collectors.toMap(
                        genre -> genre,
                        genre -> genreProbability(genre, book)
                ));
    }

    // P(genre|book)
    private double genreProbability(String genre, Book book) {
        // P(genre)
        double probabilityOfGenre = (double)trainSet.getBookCountForGenre(genre) / trainSet.getBookCount();

        double bookProbabilityForGenre = book.getWordCounts().keySet().stream()
                .mapToDouble(
                        word -> wordWeightOfWordInGenre(word, genre) * tfidf.calculate(word, book)
                )
                .sum();

        return Math.log(probabilityOfGenre) * bookProbabilityForGenre;
    }

    private double wordWeightOfWordInGenre(String word, String genre) {
        //                           1 + (sum of tfidf of word for every book in other genres)
        // word weight = log ( ------------------------------------------------------------------------- )
        //                      (num unique words) + (total sum of tfidf for all words in other genres)
        double tfidfComplementOfWord = genreWordCounts.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(genre))
                .mapToDouble(entry -> entry.getValue().getOrDefault(word, 0.0))
                .sum();

        double tfidfComplementOfAllWordsInGenre = genreTotalCounts.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(genre))
                .mapToDouble(entry -> entry.getValue())
                .sum();

        return (1 + tfidfComplementOfWord) / (totalWordCount + tfidfComplementOfAllWordsInGenre);
    }

    private void train() {
        genreWordCounts = trainSet.getGenres().stream()
                .collect(Collectors.toMap(
                        genre -> genre,
                        genre -> wordCountsForGenre(genre)
                ));

        genreTotalCounts = genreWordCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().values().stream().reduce(0.0, (a, b) -> a + b)
                ));

        totalWordCount = totalWordCount();
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

    private int totalWordCount() {
        return genreWordCounts.entrySet().stream()
                .flatMap(wordCounts -> wordCounts.getValue().keySet().stream())
                .collect(Collectors.toMap(
                        word -> word,
                        word -> 1,
                        (a, b) -> 1
                )).size();
    }
}
