package net.gangelov.bookworm.classifiers;

import net.gangelov.bookworm.Book;
import net.gangelov.bookworm.IFeature;
import net.gangelov.bookworm.features.WordFrequencies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultinomialNaiveBayes {
    private final List<String> genres;

    public MultinomialNaiveBayes(List<String> genres) {
        this.genres = genres;
    }

    public void train(Book book, String genre) {

    }

    public Map<String, Double> classify(Book book) {

    }
}
