package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataExtraction {

    public  Map<String, String> extractMetadata(String content) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("title", extractMatch(content, "Title:\\s*(.*)"));
        metadata.put("author", extractMatch(content, "Author:\\s*(.*)"));
        metadata.put("release_date", extractMatch(content, "Release date:\\s*(.*)"));
        metadata.put("language", extractMatch(content, "Language:\\s*(.*)"));
        return metadata;
    }

    private static String extractMatch(String content, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(content);
        return matcher.find() ? matcher.group(1) : "Unknown";
    }




}
