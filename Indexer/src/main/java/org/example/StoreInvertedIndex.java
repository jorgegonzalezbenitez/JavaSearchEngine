import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.example.InvertedIndexStorer;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class StoreInvertedIndex implements InvertedIndexStorer {

    private static String dbName = "BooksDatabase";
    private static String collectionName = "InvertedIndex";

    @Override
    public void storeInvertedIndexJson(Map<String, List<Map<String, String>>> invertedIndex, String outputFolderPath) {
        File baseFolder = new File(outputFolderPath + File.separator + "jsonDatamart");

        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }

        // Procesar cada palabra en el índice invertido
        invertedIndex.forEach((word, metadataList) -> {
            // Obtener el idioma del primer metadato
            String language = metadataList.get(0).get("language");
            if (language == null) {
                System.err.println("No se encontró idioma para la palabra: " + word);
                return;
            }

            // Crear carpeta del idioma
            File languageFolder = new File(baseFolder, language);
            if (!languageFolder.exists()) {
                languageFolder.mkdirs();
            }

            // Determinar el subdirectorio basado en la primera letra
            char firstChar = Character.toUpperCase(word.charAt(0));
            String subFolderName;
            if (firstChar >= 'A' && firstChar <= 'D') {
                subFolderName = "A-D";
            } else if (firstChar >= 'E' && firstChar <= 'H') {
                subFolderName = "E-H";
            } else if (firstChar >= 'I' && firstChar <= 'L') {
                subFolderName = "I-L";
            } else if (firstChar >= 'M' && firstChar <= 'P') {
                subFolderName = "M-P";
            } else if (firstChar >= 'Q' && firstChar <= 'T') {
                subFolderName = "Q-T";
            } else if (firstChar >= 'U' && firstChar <= 'Z') {
                subFolderName = "U-Z";
            } else {
                subFolderName = "Other";
            }

            // Crear el subdirectorio correspondiente dentro de la carpeta del idioma
            File subFolder = new File(languageFolder, subFolderName);
            if (!subFolder.exists()) {
                subFolder.mkdirs();
            }

            // Crear el objeto JSON para cada palabra y almacenar los metadatos
            JSONObject jsonWordObject = new JSONObject();
            JSONArray jsonArray = new JSONArray(metadataList);
            jsonWordObject.put(word, jsonArray);

            // Definir el archivo de salida dentro del subdirectorio adecuado
            File wordFile = new File(subFolder, word + ".json");

            // Guardar el JSON en el archivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(wordFile))) {
                writer.write(jsonWordObject.toString(4)); // 4 para la indentación
            } catch (IOException e) {
                System.err.println("Error writing file for word: " + word);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void storeInvertedIndexMongo(Map<String, List<Document>> invertedDict) {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(collectionName);

            invertedDict.forEach((word, books) -> {
                // Obtener el idioma del primer metadato
                String language = books.get(0).getString("language");
                if (language == null) {
                    System.err.println("No se encontró idioma para la palabra: " + word);
                    return;
                }

                // Crear el subdocumento para la palabra
                Document wordDoc = new Document("word", word).append("metadata", books);

                // Agregar el documento de la palabra al array "words" en el documento del idioma correspondiente
                Document filter = new Document("_id", language); // Documento principal por idioma
                Document update = new Document("$addToSet", new Document("words", wordDoc));

                // Realizar la operación upsert para agregar o actualizar el documento del idioma
                collection.updateOne(filter, update, new UpdateOptions().upsert(true));
            });
        } catch (Exception e) {
            System.err.println("Error storing the inverted index in MongoDB: " + e.getMessage());
            throw e;
        }
    }
}