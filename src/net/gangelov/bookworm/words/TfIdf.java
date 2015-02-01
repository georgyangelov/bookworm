package net.gangelov.bookworm.words;

import net.gangelov.bookworm.Book;
import net.gangelov.bookworm.BookTrainSet;

import java.util.Map;
import java.util.stream.Collectors;

public class TfIdf {
    private final BookTrainSet trainSet;

    public TfIdf(BookTrainSet trainSet) {
        this.trainSet = trainSet;
    }

    public double calculate(String word, Book book) {
//        Map<String, Integer> wordCountsInBook = book.getWordCounts();
//        int booksContainingWordCount = trainSet.getWordBookCount(word);
//
//        assert(booksContainingWordCount > 0);
//
//        double tf = 1 + Math.log((double)wordCountsInBook.get(word));
//        double idf = Math.log((double)trainSet.getBookCount() / booksContainingWordCount);
//
//        return tf * idf;
//        return (double)book.getWordCounts().getOrDefault(word, 0) / book.getTotalWordCount();
//        return Math.log(1 + book.getWordCounts().getOrDefault(word, 0));
        return book.getWordCounts().getOrDefault(word, 0);
    }

    public Map<String, Double> calculate(Book book) {
        return book.getWordCounts().entrySet().parallelStream()
                .collect(Collectors.toConcurrentMap(
                        Map.Entry::getKey,
                        entry -> calculate(entry.getKey(), book)
                ));
    }
}
