package org.example;

import org.json.JSONArray;

import java.io.FileNotFoundException;

public class JsonQuery implements JsonQueryProvider {
    private final CommandSet commandSet;

    public JsonQuery(String JSON_FILE_PATH) throws FileNotFoundException {
        this.commandSet = new CommandSet(JSON_FILE_PATH);
    }

    @Override
    public String searchInJsonDatamart(int option) {
        switch (option) {
            case 1:
                return commandSet.findBooksWithCommonWords(); // Puedes cambiar el retorno a String si es necesario.
            default:
                System.out.println("Invalid option");
                break;
        }
        return "";
    }
}
