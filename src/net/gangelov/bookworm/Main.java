package net.gangelov.bookworm;

import net.gangelov.bookworm.readers.EPUBReader;
import net.gangelov.bookworm.storage.WordCountSerializer;
import net.gangelov.bookworm.words.FrequencyExtractor;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

class Pair<A, B> {
    public final A first;
    public final B second;

    public Pair(A a, B b) {
        first = a;
        second = b;
    }
}

public class Main {
    public static void main(String[] args) throws Exception {
        readWordCountBinFiles();
//        generateWordCountBinFiles();
    }

    private static void readWordCountBinFiles() throws IOException {
        Map<String, Map<String, Integer>> genreWordCounts = new HashMap<>();

        String[] genres = new String[] { "cookbooks", "fantasy", "mystery", "romance", "science fiction" };

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
            Map<String, Integer> wordCounts = countWordsInDirectory(genreDirectory.getAbsolutePath());
            BufferedOutputStream out = new BufferedOutputStream(
                    new FileOutputStream(genreDirectory.getName() + ".bin")
            );

            System.out.println(genreDirectory.getName());
            WordCountSerializer.serialize(wordCounts, out);

            out.flush();
            out.close();
        }
    }

    private static Map<String, Integer> countWordsInDirectory(String directory) {
        @SuppressWarnings("unchecked")
        Collection<File> bookFiles = FileUtils.listFiles(
                new File(directory),
                new String[] { "epub" },
                true
        );

        Map<String, Integer> bookCounts = bookFiles.stream().limit(100).map((bookFile) -> {
            String bookName = bookFile.getName().replaceAll(",", "");
            System.out.println("Processing " + bookName);

            Map<String, Integer> wordCounts = null;

            try {
                wordCounts = countWords(bookFile.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return wordCounts;
        }).reduce(new HashMap<String, Integer>(), (accumulator, words) -> {
            for (Map.Entry<String, Integer> word : words.entrySet()) {
                accumulator.merge(word.getKey(), word.getValue(), (a, b) -> a + b);
            }

            return accumulator;
        });

        return bookCounts;
    }

    private static Map<String, Integer> countWords(String bookPath) throws Exception {
        EPUBReader reader = new EPUBReader(new BufferedInputStream(new FileInputStream(bookPath)));

        String content = reader.getString();
        FrequencyExtractor frequencyExtractor = new FrequencyExtractor();

        return frequencyExtractor.extractFrom(content);
    }

//    private static <K, V extends Comparable<V>> List<Map.Entry<K, V>> sortedEntryListByValue(Set<Map.Entry<K, V>> entries) {
//        List<Map.Entry<K, V>> entryList = new ArrayList<>(entries);
//
//        entryList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
//
//        return entryList;
//    }
}
