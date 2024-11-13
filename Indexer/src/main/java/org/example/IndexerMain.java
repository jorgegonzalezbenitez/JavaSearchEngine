package org.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class IndexerMain {
    private static final String datalakePath = "C:\\Users\\jorge gonzalez\\Documents\\Tercero 2024-2025\\1er Cuatri\\Big Data\\JavaSearchEngine\\SearchEngine\\DataLake";
    private static final String jsonDatamartPath = "C:\\Users\\jorge gonzalez\\Documents\\Tercero 2024-2025\\1er Cuatri\\Big Data\\JavaSearchEngine\\SearchEngine";

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        InvertedIndexBuilder invertedIndexBuilder = new BuiltInvertedIndex();
        InvertedIndexStorer invertedIndexStorer = new StoreInvertedIndex();

        System.out.println("Iniciando la indexación...");

        scheduler.scheduleAtFixedRate(() -> {
            invertedIndexStorer.storeInvertedIndexJson(invertedIndexBuilder.buildInvertedIndex(datalakePath),jsonDatamartPath);
            System.out.println("Los nuevos libros se han indexado correctamente en el JSON DATAMART");

            invertedIndexStorer.storeInvertedIndexMongo(invertedIndexBuilder.buildInvertedIndex(datalakePath));
            System.out.println("Los nuevos libros se han indexado correctamente en el Mongo DATAMART");
        }, 2, 30 , TimeUnit.SECONDS); // Ejecuta cada 20 segundos

    }

}
