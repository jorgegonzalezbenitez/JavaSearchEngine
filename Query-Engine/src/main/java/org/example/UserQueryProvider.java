package org.example;

import org.bson.json.JsonParseException;

import java.util.List;

public interface JsonQueryProvider {
    String searchInJsonDatamart(int searchType) throws JsonParseException;


}
