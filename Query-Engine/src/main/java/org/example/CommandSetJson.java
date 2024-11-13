package org.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class CommandSet {
    private final String JSON_FILE_PATH;

    public CommandSet(String JSON_FILE_PATH) throws FileNotFoundException {
        this.JSON_FILE_PATH = JSON_FILE_PATH;
    }

    private JSONArray loadJsonData(String jsonFilePath) {
        if (jsonFilePath == null) {
            System.out.println("JSON file path is null.");
            return null;
        }
        try (FileReader reader = new FileReader(jsonFilePath)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            return jsonObject.optJSONArray(jsonObject.keys().next());
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + jsonFilePath);
            return null;
        } catch (org.json.JSONException e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }

    public String findBooksWithCommonWords() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the words you wish to search (separated by commas):");
        String input = scanner.nextLine().trim();

        // Dividir las palabras por comas y eliminar espacios adicionales
        String[] words = input.split("\\s*,\\s*");

        if (words.length < 1) {
            return "Please enter at least one word separated by commas.";
        }

        // Cargar los datos para cada palabra ingresada
        List<JSONArray> wordDataArrays = new ArrayList<>();
        for (String word : words) {
            String wordPath = getJsonFilePathForWord(word, JSON_FILE_PATH);
            JSONArray wordData = loadJsonData(wordPath);
            if (wordData == null) {
                return "One of the search files could not be loaded for word: " + word;
            }
            wordDataArrays.add(wordData);
        }

        // Encontrar libros comunes y formatear la salida
        Map<String, Map<String, List<String>>> commonBooks = findAndGroupBooks(wordDataArrays, words);

        return formatOutput(commonBooks);
    }

    private Map<String, Map<String, List<String>>> findAndGroupBooks(List<JSONArray> wordDataArrays, String[] words) {
        List<Map<String, Map<String, List<String>>>> wordMaps = new ArrayList<>();

        for (JSONArray wordData : wordDataArrays) {
            Map<String, Map<String, List<String>>> bookMap = new HashMap<>();
            for (int i = 0; i < wordData.length(); i++) {
                JSONObject book = wordData.getJSONObject(i);
                String title = book.optString("title");
                String author = book.optString("author");
                String releaseDate = book.optString("release_date");
                String lineText = book.optString("line_text") + " (Line: " + book.optString("line_number") + ")";
                String key = title + " | " + author + " | " + releaseDate;

                bookMap.putIfAbsent(key, new HashMap<>());
                bookMap.get(key).putIfAbsent(words[wordMaps.size()], new ArrayList<>());
                bookMap.get(key).get(words[wordMaps.size()]).add(lineText);
            }
            wordMaps.add(bookMap);
        }

        // Identificar libros comunes
        Set<String> commonKeys = new HashSet<>(wordMaps.get(0).keySet());
        for (Map<String, Map<String, List<String>>> map : wordMaps) {
            commonKeys.retainAll(map.keySet());
        }

        // Crear estructura final solo con libros comunes
        Map<String, Map<String, List<String>>> commonBooks = new HashMap<>();
        for (String key : commonKeys) {
            Map<String, List<String>> groupedLines = new HashMap<>();
            for (int i = 0; i < words.length; i++) {
                Map<String, Map<String, List<String>>> map = wordMaps.get(i);
                if (map.containsKey(key)) {
                    groupedLines.put(words[i], map.get(key).get(words[i]));
                }
            }
            commonBooks.put(key, groupedLines);
        }

        return commonBooks;
    }

    private String formatOutput(Map<String, Map<String, List<String>>> commonBooks) {
        StringBuilder result = new StringBuilder();
        result.append("------------------------------------------------\n");

        if (commonBooks.isEmpty()) {
            result.append("No common books found.\n");
        } else {
            for (Map.Entry<String, Map<String, List<String>>> entry : commonBooks.entrySet()) {
                String[] keyParts = entry.getKey().split(" \\| ");
                String title = keyParts[0];
                String author = keyParts[1];
                String releaseDate = keyParts[2];

                result.append("Title: ").append(title).append("\n");
                result.append("Author: ").append(author).append("\n");
                result.append("Release Date: ").append(releaseDate).append("\n");
                result.append("Lines:\n");

                for (Map.Entry<String, List<String>> wordEntry : entry.getValue().entrySet()) {
                    String word = wordEntry.getKey();
                    List<String> lines = wordEntry.getValue();
                    result.append("[").append(word).append("] (").append(lines.size()).append(")\n");
                    for (String line : lines) {
                        result.append(" - ").append(line).append("\n");
                    }
                }
                result.append("------------------------------------------------\n");
            }
        }

        return result.toString();
    }

    private String getJsonFilePathForWord(String word, String basePath) {
        if (word.isEmpty()) return null;
        char firstChar = Character.toUpperCase(word.charAt(0));

        String subfolder = null;
        if (firstChar >= 'A' && firstChar <= 'D') {
            subfolder = "A-D";
        } else if (firstChar >= 'E' && firstChar <= 'H') {
            subfolder = "E-H";
        } else if (firstChar >= 'I' && firstChar <= 'L') {
            subfolder = "I-L";
        } else if (firstChar >= 'M' && firstChar <= 'P') {
            subfolder = "M-P";
        } else if (firstChar >= 'Q' && firstChar <= 'T') {
            subfolder = "Q-T";
        } else if (firstChar >= 'U' && firstChar <= 'Z') {
            subfolder = "U-Z";
        }

        if (subfolder != null) {
            return basePath + File.separator + subfolder + File.separator + word.toLowerCase() + ".json";
        }
        return null;
    }
}
