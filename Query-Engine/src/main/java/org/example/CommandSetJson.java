package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class CommandSetJson {
    private final String JSON_FILE_PATH;

    public CommandSetJson(String JSON_FILE_PATH) {
        this.JSON_FILE_PATH = JSON_FILE_PATH;
    }

    private JSONArray loadJsonData(String jsonFilePath) {
        if (jsonFilePath == null) {
            System.out.println("JSON file path is null.");
            return null;
        }
        try (FileReader reader = new FileReader(jsonFilePath)) {
            JSONObject jsonObject = new JSONObject(new JSONTokener(reader));
            return jsonObject.optJSONArray(jsonObject.keys().next());
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + jsonFilePath);
        } catch (Exception e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
        }
        return null;
    }

    public Map<String, Map<String, List<String>>> findWords() {
        System.out.println("Enter the words you wish to search (separated by commas):");
        String[] words = new Scanner(System.in).nextLine().trim().split("\\s*,\\s*");

        if (words.length < 1) return Collections.emptyMap();

        List<JSONArray> wordDataArrays = new ArrayList<>();
        for (String word : words) {
            JSONArray wordData = loadJsonData(getJsonFilePathForWord(word, JSON_FILE_PATH));
            if (wordData == null) return Collections.emptyMap();
            wordDataArrays.add(wordData);
        }
        return findAndGroupBooks(wordDataArrays, words);
    }

    private Map<String, Map<String, List<String>>> findAndGroupBooks(List<JSONArray> wordDataArrays, String[] words) {
        List<Map<String, Map<String, List<String>>>> wordMaps = new ArrayList<>();
        for (int i = 0; i < wordDataArrays.size(); i++) {
            Map<String, Map<String, List<String>>> bookMap = new HashMap<>();
            for (Object obj : wordDataArrays.get(i)) {
                JSONObject book = (JSONObject) obj;
                String key = book.optString("title") + " | " + book.optString("author") + " | " + book.optString("release_date");
                bookMap.computeIfAbsent(key, k -> new HashMap<>())
                        .computeIfAbsent(words[i], k -> new ArrayList<>())
                        .add(book.optString("line_text") + " (Line: " + book.optString("line_number") + ")");
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

    private String getJsonFilePathForWord(String word, String basePath) {
        if (word.isEmpty()) return null;
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
