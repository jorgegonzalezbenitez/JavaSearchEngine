package testIndexer;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openjdk.jmh.annotations.*;
import org.example.BuiltInvertedIndex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
public class IndexerTest {

    private static final String DB_NAME = "BooksDatabaseTest";
    private static final String COLLECTION_NAME = "InvertedIndexTest";
    private static final String DATALAKE_PATH = "C:\\Users\\jorge gonzalez\\Documents\\Tercero 2024-2025\\1er Cuatri\\Big Data\\JavaSearchEngine\\SearchEngine\\Datalake"; // Cambia a la ruta real del datalake

    private Map<String, List<Document>> invertedDict;
    private String outputFolderPath;

    @Setup(Level.Trial)
    public void setup() {
        BuiltInvertedIndex indexBuilder = new BuiltInvertedIndex();
        invertedDict = indexBuilder.buildInvertedIndex(DATALAKE_PATH);

        outputFolderPath = "outputJsonTest";
    }

    @Benchmark
    public void storeInvertedIndexJson() {
        File baseFolder = new File(outputFolderPath, "jsonDatamartTest");
        if (!baseFolder.exists()) baseFolder.mkdirs();

        invertedDict.forEach((word, metadataList) -> {
            String language = metadataList.get(0).getString("language");
            if (language == null) {
                System.err.println("No se encontró idioma para la palabra: " + word);
                return;
            }
            File subFolder = new File(new File(baseFolder, language), getSubfolderName(word));
            if (!subFolder.exists()) subFolder.mkdirs();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(subFolder, word + ".json")))) {
                JSONObject jsonWordObject = new JSONObject().put(word, new JSONArray(metadataList));
                writer.write(jsonWordObject.toString(4));
            } catch (IOException e) {
                System.err.println("Error writing file for word: " + word);
                e.printStackTrace();
            }
        });
    }

    @Benchmark
    public void storeInvertedIndexMongo() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = mongoClient.getDatabase(DB_NAME);
            MongoCollection<Document> collection = db.getCollection(COLLECTION_NAME);

            invertedDict.forEach((word, books) -> {
                String language = books.get(0).getString("language");
                if (language == null) {
                    System.err.println("No se encontró idioma para la palabra: " + word);
                    return;
                }

                Document filter = new Document("_id", language);
                Document update = new Document("$addToSet", new Document("words", new Document("word", word).append("metadata", books)));

                collection.updateOne(filter, update, new UpdateOptions().upsert(true));
            });
        } catch (Exception e) {
            System.err.println("Error storing the inverted index in MongoDB: " + e.getMessage());
            throw e;
        }
    }

    private String getSubfolderName(String word) {
        char firstChar = Character.toUpperCase(word.charAt(0));
        if (firstChar >= 'A' && firstChar <= 'D') return "A-D";
        else if (firstChar >= 'E' && firstChar <= 'H') return "E-H";
        else if (firstChar >= 'I' && firstChar <= 'L') return "I-L";
        else if (firstChar >= 'M' && firstChar <= 'P') return "M-P";
        else if (firstChar >= 'Q' && firstChar <= 'T') return "Q-T";
        else if (firstChar >= 'U' && firstChar <= 'Z') return "U-Z";
        else return "Other";
    }
}
