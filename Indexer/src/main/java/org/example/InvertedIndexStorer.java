package org.example;

import com.mongodb.MongoException;
import org.bson.Document;
import org.bson.json.JsonParseException;

import java.util.List;
import java.util.Map;

public interface InvertedIndexStorer {
    void storeInvertedIndexMongo(Map<String, List<Document>> invertedDict)throws MongoException;
    void storeInvertedIndexJson(Map<String, List<Document>> invertedDict, String outputFile)throws JsonParseException;
}