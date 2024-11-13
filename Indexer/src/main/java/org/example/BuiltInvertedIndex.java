package org.example;

import org.bson.Document;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BuiltInvertedIndexMongo implements MongoInverted{


    MetadataExtraction metadataExtraction = new MetadataExtraction();

    private static final List<String> stopWords = Arrays.asList(
            "the", "and", "is", "in", "it", "of", "to", "a", "that", "with", "for", "as",
            "on", "was", "at", "by", "an", "be", "this", "which", "or", "from", "but",
            "not", "are", "have", "has", "had", "were", "they", "them", "their", "you",
            "yours", "us", "our"
    );


    @Override
    public Map<String, List<Document>> builtInvertedIndexMongo(String datalake) {
        Map<String, List<Document>> invertedDict = new HashMap<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(datalake), "book_*.txt")) {
            for (Path path : directoryStream) {
                try {
                    String content = Files.readString(path);
                    Map<String, String> metadata = metadataExtraction.extractMetadata(content);
                    Document bookEntry = new Document(metadata);

                    List<String> words = Arrays.stream(content.toLowerCase().split("\\W+"))
                            .filter(word -> !stopWords.contains(word) && !word.matches("\\d+"))
                            .collect(Collectors.toList());

                    for (String word : words) {
                        invertedDict.putIfAbsent(word, new ArrayList<>());
                        List<Document> bookList = invertedDict.get(word);
                        if (!bookList.contains(bookEntry)) {
                            bookList.add(bookEntry);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error processing file " + path.getFileName() + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading datalake path: " + e.getMessage());
        }

        return invertedDict;
    }

}
