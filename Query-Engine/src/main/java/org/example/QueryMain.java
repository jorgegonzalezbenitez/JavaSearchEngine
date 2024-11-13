package org.example;

import java.io.FileNotFoundException;
import java.util.Scanner;

public class QueryMain {
    private static final String JSON_FILE_PATH = "C:\\Users\\jorge gonzalez\\Documents\\Tercero 2024-2025\\1er Cuatri\\Big Data\\JavaSearchEngine\\SearchEngine\\jsonDatamart";
    private static final String MONGO_URL = "mongodb://localhost:27017";

    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Select the language for the search (English, Spanish, French ...): ");
        String languageChoice = scanner.nextLine().trim();

        String languagePath = JSON_FILE_PATH + "\\" + languageChoice;
        UserQueryProvider userQueryProvider = new UserQuery(languagePath, MONGO_URL, languageChoice);

        while (true) {
            System.out.println("1. Search for words that are in common books");
            System.out.println("2. End the program");
            System.out.print("Enter your choice (1 or 2): ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("1")) {
                String output = userQueryProvider.searchInJsonDatamart(choice);
                System.out.println(output);
            } else if (choice.equals("2")) {
                System.out.println("Closing the program...");
                break;
            } else {
                System.out.println("Invalid option. Please enter 1 or 2.");
            }
        }
        scanner.close();
    }
}
