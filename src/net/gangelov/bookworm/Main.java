package net.gangelov.bookworm;

import net.gangelov.bookworm.readers.FB2Reader;
import org.apache.commons.io.FileUtils;

import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        FB2Reader reader = new FB2Reader(new BufferedInputStream(new FileInputStream("../lotr.fb2")));

        String content = reader.getString();

        FileUtils.writeStringToFile(new File("output.txt"), content, "UTF-8");
    }
}
