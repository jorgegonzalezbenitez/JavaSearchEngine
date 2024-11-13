package org.example;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public class UserQuery implements UserQueryProvider {
    private final CommandSetJson commandSetJson;
    private final CommandSetMongo commandSetMongo;
    private final String language;

    public UserQuery(String languagePath, String url, String language) throws FileNotFoundException {
        this.commandSetJson = new CommandSetJson(languagePath);
        this.commandSetMongo = new CommandSetMongo(url);
        this.language = language;
    }

    @Override
    public String searchInJsonDatamart(String option) {
        switch (option) {
            case "1":
                Map<String, Map<String, List<String>>> commonBooksJson = commandSetJson.findWords();
                return formatOutput(commonBooksJson);
            default:
                System.out.println("Invalid option");
                break;
        }
        return "";
    }

    @Override
    public String searchInMongoDatamart(String option) {
        switch (option) {
            case "1":
                Map<String, Map<String, List<String>>> commonBooksMongo = commandSetMongo.findWords(language);
                return formatOutput(commonBooksMongo);
            default:
                System.out.println("Invalid option");
                break;
        }
        return "";
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
}
