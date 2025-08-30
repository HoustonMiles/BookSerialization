package com.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class BookUtils {

    public static void serialize(Set<Book> books, String filename) {
        List<String> lines = new ArrayList<>();
        lines.add("Title,Author,YearPublished"); // CSV header

        for (Book book : books) {
            lines.add(String.format("%s,%s,%d",
                    escape(book.getTitle()),
                    escape(book.getAuthor()),
                    book.getYearPublished()));
        }

        try {
            Files.write(Paths.get(filename), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Books saved to CSV: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<Book> deserialize(String filename) {
        Set<Book> books = new HashSet<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length == 3) {
                    String title = unescape(parts[0]);
                    String author = unescape(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    books.add(new Book(title, author, year));
                }
            }
            System.out.println("Books loaded from CSV: " + filename);
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return books;
    }

    // Handle commas and quotes in fields
    private static String escape(String field) {
        if (field.contains(",") || field.contains("\"")) {
            field = field.replace("\"", "\"\"");
            return "\"" + field + "\"";
        }
        return field;
    }

    private static String unescape(String field) {
        if (field.startsWith("\"") && field.endsWith("\"")) {
            field = field.substring(1, field.length() - 1).replace("\"\"", "\"");
        }
        return field;
    }
}
