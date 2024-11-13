package org.example;

import org.bson.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class BuiltInvertedIndex implements InvertedIndexBuilder {

    MetadataExtraction metadataExtraction = new MetadataExtraction();

    private static final CharArraySet stopWords = (CharArraySet) StandardAnalyzer.STOP_WORDS_SET;

    @Override
    public Map<String, List<Document>> buildInvertedIndex(String datalake) {
        Map<String, List<Document>> invertedIndex = new HashMap<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(datalake), "book_*.txt")) {
            for (Path path : directoryStream) {
                try (BufferedReader reader = Files.newBufferedReader(path)) {
                    String line;
                    int lineNumber = 0;
                    Map<String, String> metadata = metadataExtraction.extractMetadata(Files.readString(path));

                    while ((line = reader.readLine()) != null) {
                        lineNumber++;
                        String[] words = line.toLowerCase().split("\\W+");

                        for (String word : words) {
                            if (!stopWords.contains(word) && word.matches("^[a-zA-Z].*") && word.length() > 1) {
                                invertedIndex.putIfAbsent(word, new ArrayList<>());

                                Document wordEntry = new Document(metadata)
                                        .append("line_number", lineNumber)
                                        .append("line_text", line.trim());

                                List<Document> wordList = invertedIndex.get(word);
                                if (!wordList.contains(wordEntry)) {
                                    wordList.add(wordEntry);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error processing file " + path.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading datalake path: " + e.getMessage());
        }

        return invertedIndex;
    }
}