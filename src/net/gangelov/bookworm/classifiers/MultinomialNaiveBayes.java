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
    private Map<String, Map<String, Double>> genreWordCounts;

    // {genre: sum of tfidf for all words}
    private Map<String, Double> genreTotalCounts;

    private final BookTrainSet trainSet;
    private final TfIdf tfidf;

    public MultinomialNaiveBayes(BookTrainSet trainSet) {
        this.trainSet = trainSet;
        this.tfidf = new TfIdf(trainSet);

        train();
    }

    public String classify(Book book) {
        Map<String, Double> weights = classificationWeights(book);

        return weights.entrySet().stream().max((a, b) -> a.getValue().compareTo(b.getValue())).get().getKey();
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
//        double probabilityOfGenre = (double)trainSet.getBookCountForGenre(genre) / trainSet.getBookCount();

        // P(book|genre) = product { P(word|genre)^(word tfidf in book) }
        //               ~ sum { log( P(word|genre) )*(word tfidf in book) }
        double bookProbabilityForGenre = book.getWordCounts().keySet().stream()
                .mapToDouble(
                        word -> Math.log(probabilityOfWordInGenre(word, genre)) * tfidf.calculate(word, book)
                )
                .sum();

        return bookProbabilityForGenre;
    }

    // P(word|genre)
    private double probabilityOfWordInGenre(String word, String genre) {
        //                       1 + (sum of tfidf of word for every book in genre)
        // P(word|genre) = ----------------------------------------------------------------
        //                 (num unique words) + (total sum of tfidf for all words in genre)
        Map<String, Double> wordsInGenre = genreWordCounts.get(genre);

        double tfidfOfWordInGenre = wordsInGenre.getOrDefault(word, 0.0);
        double tfidfOfAllWordsInGenre = genreTotalCounts.get(genre);
        double uniqueWordCount = wordsInGenre.size();

        return (1 + tfidfOfWordInGenre) / (uniqueWordCount + tfidfOfAllWordsInGenre);
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
}
