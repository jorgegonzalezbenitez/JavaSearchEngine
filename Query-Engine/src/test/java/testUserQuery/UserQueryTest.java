package testUserQuery;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class UserQueryTest {

    private static final String LANGUAGE_PATH = "C:\\Users\\jorge gonzalez\\Documents\\Tercero 2024-2025\\1er Cuatri\\Big Data\\JavaSearchEngine\\SearchEngine\\jsonDatamart\\English";
    private static final String MONGO_URL = "mongodb://localhost:27017";
    private static final String LANGUAGE = "English";
    private static final MongoClient mongoClient = MongoClients.create(MONGO_URL);
    private static final MongoCollection<Document> collection = mongoClient.getDatabase("BooksDatabase").getCollection("InvertedIndex");

    private List<String> words = Arrays.asList("abandon", "find", "because");

    @Param({"1", "2", "3"})
    private int wordCount;

    @Benchmark
    public void benchmarkSearchInJsonDatamart() {
        List<String> wordsSubset = words.subList(0, wordCount);
        findAndGroupBooks(loadWordData(wordsSubset, true), wordsSubset);
    }

    @Benchmark
    public void benchmarkSearchInMongoDatamart() {
        List<String> wordsSubset = words.subList(0, wordCount);
        findAndGroupBooks(loadWordData(wordsSubset, false), wordsSubset);
    }

    private List<List<Map<String, String>>> loadWordData(List<String> words, boolean fromJson) {
        List<List<Map<String, String>>> wordDataLists = new ArrayList<>();
        for (String word : words) {
            List<Map<String, String>> wordData = fromJson ? loadWordDataFromJson(word) : loadWordDataFromMongo(word.toLowerCase());
            if (wordData == null) return Collections.emptyList();
            wordDataLists.add(wordData);
        }
        return wordDataLists;
    }

    private List<Map<String, String>> loadWordDataFromJson(String word) {
        JSONArray wordData = loadJsonData(getJsonFilePathForWord(word, LANGUAGE_PATH));
        if (wordData == null) return null;

        List<Map<String, String>> data = new ArrayList<>();
        for (Object obj : wordData) {
            JSONObject book = (JSONObject) obj;
            Map<String, String> entry = new HashMap<>();
            entry.put("title", book.optString("title", "Unknown"));
            entry.put("author", book.optString("author", "Unknown"));
            entry.put("release_date", book.optString("release_date", "Unknown"));
            entry.put("line_text", book.optString("line_text"));
            entry.put("line_number", book.optString("line_number", "Unknown"));
            data.add(entry);
        }
        return data;
    }

    private List<Map<String, String>> loadWordDataFromMongo(String word) {
        Document languageDoc = collection.find(new Document("_id", LANGUAGE)).first();
        if (languageDoc == null) return null;

        for (Document wordDoc : (List<Document>) languageDoc.get("words")) {
            if (wordDoc.getString("word").equalsIgnoreCase(word)) {
                List<Map<String, String>> data = new ArrayList<>();
                for (Document book : (List<Document>) wordDoc.get("metadata")) {
                    Map<String, String> entry = new HashMap<>();
                    entry.put("title", book.getString("title") != null ? book.getString("title") : "Unknown");
                    entry.put("author", book.getString("author") != null ? book.getString("author") : "Unknown");
                    entry.put("release_date", book.getString("release_date") != null ? book.getString("release_date") : "Unknown");
                    entry.put("line_text", book.getString("line_text") != null ? book.getString("line_text") : "Unknown");
                    entry.put("line_number", book.get("line_number") != null ? String.valueOf(book.get("line_number")) : "Unknown");
                    data.add(entry);
                }
                return data;
            }
        }
        return null;
    }

    private Map<String, Map<String, List<String>>> findAndGroupBooks(List<List<Map<String, String>>> wordDataLists, List<String> words) {
        List<Map<String, Map<String, List<String>>>> wordMaps = new ArrayList<>();
        for (int i = 0; i < wordDataLists.size(); i++) {
            Map<String, Map<String, List<String>>> bookMap = new HashMap<>();
            for (Map<String, String> book : wordDataLists.get(i)) {
                String key = String.join(" | ", book.get("title"), book.get("author"), book.get("release_date"));
                bookMap.computeIfAbsent(key, k -> new HashMap<>())
                        .computeIfAbsent(words.get(i), k -> new ArrayList<>())
                        .add(book.get("line_text") + " (Line: " + book.get("line_number") + ")");
            }
            wordMaps.add(bookMap);
        }

        Set<String> commonKeys = new HashSet<>(wordMaps.get(0).keySet());
        wordMaps.forEach(map -> commonKeys.retainAll(map.keySet()));

        Map<String, Map<String, List<String>>> commonBooks = new HashMap<>();
        for (String key : commonKeys) {
            Map<String, List<String>> groupedLines = new HashMap<>();
            for (int i = 0; i < words.size(); i++) {
                if (wordMaps.get(i).containsKey(key)) {
                    groupedLines.put(words.get(i), wordMaps.get(i).get(key).get(words.get(i)));
                }
            }
            commonBooks.put(key, groupedLines);
        }
        return commonBooks;
    }

    private JSONArray loadJsonData(String jsonFilePath) {
        try (FileReader reader = new FileReader(jsonFilePath)) {
            JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
            return jsonObject.optJSONArray(jsonObject.keys().next());
        } catch (Exception e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
            return null;
        }
    }

    private String getJsonFilePathForWord(String word, String basePath) {
        char firstChar = Character.toUpperCase(word.charAt(0));
        String subfolder = switch (firstChar) {
            case 'A', 'B', 'C', 'D' -> "A-D";
            case 'E', 'F', 'G', 'H' -> "E-H";
            case 'I', 'J', 'K', 'L' -> "I-L";
            case 'M', 'N', 'O', 'P' -> "M-P";
            case 'Q', 'R', 'S', 'T' -> "Q-T";
            case 'U', 'V', 'W', 'X', 'Y', 'Z' -> "U-Z";
            default -> null;
        };
        return subfolder != null ? basePath + File.separator + subfolder + File.separator + word.toLowerCase() + ".json" : null;
    }
}
