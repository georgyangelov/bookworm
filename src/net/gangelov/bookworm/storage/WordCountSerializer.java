package net.gangelov.bookworm.storage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class WordCountSerializer {
    public static void serialize(Map<String, Integer> wordCounts, OutputStream stream) throws IOException {
        DataOutputStream writer = new DataOutputStream(stream);

        writer.writeInt(wordCounts.size());

        for (Map.Entry<String, Integer> word : wordCounts.entrySet()) {
            writer.writeUTF(word.getKey());
            writer.writeInt(word.getValue());
        }
    }

    public static Map<String, Integer> deserialize(InputStream stream) throws IOException {
        DataInputStream reader = new DataInputStream(stream);
        Map<String, Integer> wordCounts = new HashMap<>();

        int wordsCount = reader.readInt();

        for (int i = 0; i < wordsCount; i++) {
            String word = reader.readUTF();
            int count = reader.readInt();

            wordCounts.put(word, count);
        }

        return wordCounts;
    }
}
