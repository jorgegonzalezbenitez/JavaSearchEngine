package org.example;

import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.json.JsonParseException;

import java.util.List;
import java.util.Map;

public interface InvertedIndexBuilder {
    Map<String, List<Document>> buildInvertedIndex(String datalakle)throws MongoException;
}