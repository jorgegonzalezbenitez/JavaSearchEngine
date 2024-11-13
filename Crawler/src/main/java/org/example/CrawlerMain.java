package org.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CrawlerMain {
    private  static String datalakePath = "C:\\Users\\jorge gonzalez\\Documents\\Tercero 2024-2025\\1er Cuatri\\Big Data\\JavaSearchEngine\\SearchEngine";
    public static void main(String[] args) {
        Crawler crawler = new Crawler(datalakePath);
        System.out.println("Ininciando la descarga de libros...\n");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {
            crawler.crawlerRunner();
        }, 1, 60, TimeUnit.SECONDS);
    }
}