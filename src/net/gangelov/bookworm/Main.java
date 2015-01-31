package net.gangelov.bookworm;

import net.gangelov.bookworm.classifiers.MultinomialNaiveBayes;
import net.gangelov.bookworm.readers.EPUBReader;
import net.gangelov.bookworm.storage.WordCountSerializer;
import net.gangelov.bookworm.words.FrequencyExtractor;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {
    static final String[] genres = new String[] { "cookbooks", "fantasy", "mystery", "romance", "science fiction" };

    public static void main(String[] args) throws Exception {
//        generateWordBookCounts();
//        readWordBookCounts();
//        generateWordCountBinFiles();
//        readWordCountBinFiles();
//        trainClassifier();
        BookTrainSet trainSet = createTrainSet();

        System.out.println("All books digested");

//        MultinomialNaiveBayes classifier = new MultinomialNaiveBayes(trainSet);
        crossValidation(10, trainSet);
    }

    private static void crossValidation(int folds, BookTrainSet trainSet) {
        int successes = trainSet.crossValidation(folds).stream()
                .mapToInt(crossValidationEntry -> {
                    MultinomialNaiveBayes classifier = new MultinomialNaiveBayes(crossValidationEntry.getTrainSet());

                    return (int) crossValidationEntry.getTestSet().parallelStream()
                            .filter(book -> {
                                String predictedGenre = classifier.classify(book);

                                return book.getGenre().equals(predictedGenre);
                            })
                            .count();
                })
                .sum();

        double accuracy = (double)successes / trainSet.getBookCount();

        System.out.println("Accuracy: " + accuracy);
    }

    private static BookTrainSet createTrainSet() {
        final File directory = new File("C:\\books");
        final File[] subdirectories = directory.listFiles(File::isDirectory);
        BookTrainSet trainSet = new BookTrainSet();

        for (File genreDirectory : subdirectories) {
            trainSet = trainSet.addFromDirectory(genreDirectory.getName(), genreDirectory);
        }

        return trainSet;
    }

//        List<Map.Entry<String, Double>> fantasyTopRelevantWords = classifier.genreWordCounts.get("fantasy").entrySet().stream()
//                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
//                .collect(Collectors.toList());

//    private static void trainClassifierFromDirectory(MultinomialNaiveBayes classifier, String genre, File directory) {
//        @SuppressWarnings("unchecked")
//        Collection<File> bookFiles = FileUtils.listFiles(
//                directory,
//                new String[] { "epub" },
//                true
//        );
//
//        bookFiles.forEach((bookFile) -> {
//            System.out.println("Processing " + bookFile.getName());
//
//            try {
//                classifier.train(Book.fromEPUB(bookFile.getAbsolutePath()), genre);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }

    private static Map<String, Integer> readWordBookCounts() throws IOException {
        Map<String, Integer> wordBookCounts = WordCountSerializer.deserialize(new BufferedInputStream(
                new FileInputStream("wordBookCounts.bin")
        ));

        System.out.println("Word book counts read");

        return wordBookCounts;
    }

    private static void generateWordBookCounts() throws IOException {
        final File directory = new File("F:\\books");

        File[] subdirectories = directory.listFiles(File::isDirectory);

        final Map<String, Integer> wordBookCounts = new HashMap<>();

        for (File genreDirectory : subdirectories) {
            Map<String, Integer> wordCounts = countWordsInDirectory(genreDirectory.getAbsolutePath(), (file) -> {
                Map<String, Integer> counts = countWords(file);

                counts.replaceAll((k, v) -> 1);

                return counts;
            });

            wordCounts.forEach((word, wordCount) -> {
                wordBookCounts.merge(word, wordCount, (a, b) -> a + b);
            });
        }

        BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream("wordBookCounts.bin")
        );

        WordCountSerializer.serialize(wordBookCounts, out);

        out.flush();
        out.close();
    }

    private static void readWordCountBinFiles() throws IOException {
        Map<String, Map<String, Integer>> genreWordCounts = new HashMap<>();

        for (String genre : genres) {
            Map<String, Integer> wordCounts = WordCountSerializer.deserialize(new BufferedInputStream(
                    new FileInputStream(genre + ".bin")
            ));

            genreWordCounts.put(genre, wordCounts);
        }

        System.out.println("Word counts read");
    }

    private static void generateWordCountBinFiles() throws IOException {
        final File directory = new File("F:\\books");

        File[] subdirectories = directory.listFiles(File::isDirectory);

        for (File genreDirectory : subdirectories) {
            Map<String, Integer> wordCounts = countWordsInDirectory(genreDirectory.getAbsolutePath(), Main::countWords);
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(genreDirectory.getName() + ".bin")
            );

            System.out.println(genreDirectory.getName());
            WordCountSerializer.serialize(wordCounts, out);

            out.flush();
            out.close();
        }
    }

    private static Map<String, Integer> countWordsInDirectory(String directory, Function<String, Map<String, Integer>> mapper) {
        @SuppressWarnings("unchecked")
        Collection<File> bookFiles = FileUtils.listFiles(
                new File(directory),
                new String[] { "epub" },
                true
        );

        Map<String, Integer> bookCounts = bookFiles.stream().map((bookFile) -> {
            String bookName = bookFile.getName().replaceAll(",", "");
            System.out.println("Processing " + bookName);

            return mapper.apply(bookFile.getAbsolutePath());
        }).reduce(new HashMap<>(), (accumulator, words) -> {
            for (Map.Entry<String, Integer> word : words.entrySet()) {
                accumulator.merge(word.getKey(), word.getValue(), (a, b) -> a + b);
            }

            return accumulator;
        });

        return bookCounts;
    }

    private static Map<String, Integer> countWords(String bookPath) {
        try {
            EPUBReader reader = new EPUBReader(new BufferedInputStream(new FileInputStream(bookPath)));

            String content = reader.getString();
            FrequencyExtractor frequencyExtractor = new FrequencyExtractor();

            return frequencyExtractor.extractFrom(content);
        } catch(Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

//    private static <K, V extends Comparable<V>> List<Map.Entry<K, V>> sortedEntryListByValue(Set<Map.Entry<K, V>> entries) {
//        List<Map.Entry<K, V>> entryList = new ArrayList<>(entries);
//
//        entryList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
//
//        return entryList;
//    }
}
