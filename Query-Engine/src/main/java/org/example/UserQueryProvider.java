package org.example;

import org.bson.json.JsonParseException;

public interface UserQueryProvider {
    String searchInJsonDatamart(String option) throws JsonParseException;


    String searchInMongoDatamart(String option);
}
