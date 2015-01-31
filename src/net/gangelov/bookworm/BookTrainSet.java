package net.gangelov.bookworm;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BookTrainSet {
    static class CrossValidationEntry {
        private final BookTrainSet trainSet;
        private final List<Book> testSet;

        public CrossValidationEntry(BookTrainSet trainSet, List<Book> testSet) {
            this.trainSet = trainSet;
            this.testSet = testSet;
        }

        public BookTrainSet getTrainSet() {
            return trainSet;
        }

        public List<Book> getTestSet() {
            return testSet;
        }
    }

    private final List<Book> books;

    // {word: number of books containing it}
    private Map<String, Integer> wordBookCounts;

    // {genre: number of books}
    private Map<String, Integer> genreBookCount;

    public BookTrainSet() {
        books = new ArrayList<>();

        wordBookCounts = new HashMap<>();
        genreBookCount = new HashMap<>();
    }

    private BookTrainSet(final List<Book> books) {
        this.books = books;

        wordBookCounts = extractWordBookCounts(books);
        genreBookCount = extractGenreBookCount();
    }

    public List<CrossValidationEntry> crossValidation(int folds) {
        int testSetSize = books.size() / folds;
        int trainSetSize = books.size() - testSetSize;

        // Shuffle books
        List<Book> shuffledBooks = new ArrayList<>(books.size());
        shuffledBooks.addAll(books);

        Collections.shuffle(shuffledBooks);

        List<CrossValidationEntry> crossValidationEntries = new ArrayList<>(folds);
        for (int i = 0; i < folds; i++) {
            List<Book> trainSetBooks = new ArrayList<>(trainSetSize);
            trainSetBooks.addAll(
                    shuffledBooks.subList(0, i * testSetSize)
            );
            trainSetBooks.addAll(
                    shuffledBooks.subList((i + 1) * testSetSize, shuffledBooks.size())
            );

            crossValidationEntries.add(
                    new CrossValidationEntry(
                            new BookTrainSet(trainSetBooks),
                            shuffledBooks.subList(i * testSetSize, (i + 1) * testSetSize)
                    )
            );
        }

        return crossValidationEntries;
    }

    public Set<String> getGenres() {
        return genreBookCount.keySet();
    }

    public int getBookCount() {
        return books.size();
    }

    public int getBookCountForGenre(String genre) {
        return genreBookCount.size();
    }

    public int getWordBookCount(String word) {
        return wordBookCounts.getOrDefault(word, 0);
    }

    public List<Book> booksForGenre(final String genre) {
        return books.stream().filter(book -> book.getGenre() == genre).collect(Collectors.toList());
    }

    public BookTrainSet addFromDirectory(String genre, File directory) {
        Collection<File> bookFiles = FileUtils.listFiles(directory, new String[]{"epub"}, true);

        List<Book> newBooks = bookFiles.parallelStream()
                .map(bookFile -> {
                    try {
                        System.out.println("Reading " + bookFile.getName());
                        return Book.fromEPUB(bookFile.getAbsolutePath(), genre);
                    } catch (Exception e) {
                        System.err.println("Cannot read book " + bookFile.getName());
                        return null;
                    }
                })
                .filter(book -> book != null)
                .collect(Collectors.toList());

        newBooks.addAll(0, books);

        return new BookTrainSet(newBooks);
    }

    // {word: number of books containing it}
    private Map<String, Integer> extractWordBookCounts(List<Book> bookSubset) {
        return bookSubset.parallelStream()
                .map(Book::getWordCounts)
                .filter(obj -> obj != null)
                .flatMap(wordCounts -> wordCounts.keySet().stream())
                .collect(Collectors.toMap(
                        word -> word,
                        word -> 1,
                        (c1, c2) -> c1 + c2
                ));
    }

    // {genre: number of books}
    private Map<String, Integer> extractGenreBookCount() {
        return books.stream()
                .collect(Collectors.groupingBy(
                        book -> book.getGenre(),
                        Collectors.summingInt(book -> 1)
                ));
    }
}
