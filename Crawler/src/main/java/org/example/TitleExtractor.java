package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TitleExtractor {
    public String readContent(String content, int nLines) {
        String[] lines = content.split(System.lineSeparator());
        Pattern titlePattern = Pattern.compile("Title:\\s*(.*)");

        for (int i = 0; i < Math.min(nLines, lines.length); i++) {
            String line = lines[i];
            Matcher matcher = titlePattern.matcher(line);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }

        return null;
    }

}
