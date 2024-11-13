package org.example;

import com.mongodb.client.*;
import org.bson.Document;

import java.util.*;

public class CommandSetMongo {
    private final MongoCollection<Document> collection;

    public CommandSetMongo(String url) {
        MongoClient mongoClient = MongoClients.create(url);
        collection = mongoClient.getDatabase("BooksDatabase").getCollection("InvertedIndex");
    }

    public Map<String, Map<String, List<String>>> findWords(String language) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the words you wish to search (separated by commas):");
        String[] words = scanner.nextLine().trim().split("\\s*,\\s*");

        if (words.length < 1) return Collections.emptyMap();

        List<List<Document>> wordDataLists = new ArrayList<>();
        for (String word : words) {
            List<Document> wordData = loadWordDataFromMongo(language, word.toLowerCase());
            if (wordData == null) return Collections.emptyMap();
            wordDataLists.add(wordData);
        }
        return findAndGroupBooks(wordDataLists, words);
    }

    private List<Document> loadWordDataFromMongo(String language, String word) {
        Document languageDoc = collection.find(new Document("_id", language)).first();
        if (languageDoc == null) return null;

        for (Document wordDoc : (List<Document>) languageDoc.get("words")) {
            if (wordDoc.getString("word").equalsIgnoreCase(word)) {
                return (List<Document>) wordDoc.get("metadata");
            }
        }
        return null;
    }

    private Map<String, Map<String, List<String>>> findAndGroupBooks(List<List<Document>> wordDataLists, String[] words) {
        List<Map<String, Map<String, List<String>>>> wordMaps = new ArrayList<>();

        for (int wIndex = 0; wIndex < wordDataLists.size(); wIndex++) {
            Map<String, Map<String, List<String>>> bookMap = new HashMap<>();
            for (Document book : wordDataLists.get(wIndex)) {
                String title = book.getString("title");
                String author = book.getString("author");
                String releaseDate = book.getString("release_date");
                String lineText = book.getString("line_text") + " (Line: " +
                        (book.containsKey("line_number") ? book.get("line_number") : "Unknown") + ")";

                String key = (title != null ? title : "Unknown") + " | " +
                        (author != null ? author : "Unknown") + " | " +
                        (releaseDate != null ? releaseDate : "Unknown");

                bookMap.computeIfAbsent(key, k -> new HashMap<>())
                        .computeIfAbsent(words[wIndex], k -> new ArrayList<>())
                        .add(lineText);
            }
            wordMaps.add(bookMap);
        }

        Set<String> commonKeys = new HashSet<>(wordMaps.get(0).keySet());
        wordMaps.forEach(map -> commonKeys.retainAll(map.keySet()));

        Map<String, Map<String, List<String>>> commonBooks = new HashMap<>();
        for (String key : commonKeys) {
            Map<String, List<String>> groupedLines = new HashMap<>();
            for (int i = 0; i < words.length; i++) {
                if (wordMaps.get(i).containsKey(key)) {
                    groupedLines.put(words[i], wordMaps.get(i).get(key).get(words[i]));
                }
            }
            commonBooks.put(key, groupedLines);
        }

        return commonBooks;
    }
}
