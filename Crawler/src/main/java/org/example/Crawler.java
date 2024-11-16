package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Crawler {
    private static int currentBookId = 1;
    private final TitleExtractor titleExtractor;
    private final String datalakePath;

    public Crawler(String datalakePath) {
        this.titleExtractor = new TitleExtractor();
        this.datalakePath = datalakePath;
    }

    public void crawlerRunner() {
        File outputFolder = new File(datalakePath, "Datalake");
        if (!outputFolder.exists()) outputFolder.mkdirs();

        List<String> downloadedTitles = new ArrayList<>();
        int downloadedBook = 0;

        while (downloadedBook < 4) {
            String urlString = String.format("https://www.gutenberg.org/cache/epub/%d/pg%d.txt", currentBookId, currentBookId);
            try {
                String response = getResponseFromUrl(urlString);
                String title = titleExtractor.readContent(response, 40);

                if (title != null && saveBook(response, title, outputFolder)) {
                    downloadedTitles.add(title);
                    downloadedBook++;
                }
            } catch (IOException e) {
                System.err.println("Error al descargar el libro con ID " + currentBookId + ": " + e.getMessage());
            }
            currentBookId++;
        }
        System.out.println("Libros descargados:");
        downloadedTitles.forEach(System.out::println);
    }

    private String getResponseFromUrl(String urlString) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (Scanner scanner = new Scanner(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                StringBuilder content = new StringBuilder();
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine()).append(System.lineSeparator());
                }
                return content.toString();
            }
        } else {
            throw new IOException("Failed to get response from the URL: " + urlString + " (HTTP status: " + connection.getResponseCode() + ")");
        }
    }

    private boolean saveBook(String content, String title, File outputFolder) {
        String sanitizedTitle = title.replaceAll("[\\\\/:*?\"<>|]", "_");
        File outputFile = new File(outputFolder, "book_" + sanitizedTitle + ".txt");

        // Usar OutputStreamWriter con codificación UTF-8
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8"))) {
            writer.write(content);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing file for title: " + title);
            return false;
        }
    }

}