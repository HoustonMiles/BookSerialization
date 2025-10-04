package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class BookUtils {
    public void serializeToCSV(Set<Book> books, String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("Title,Author,YearPublished,ISBN"); // CSV header

        for (Book book: books) { // Skip header
            String line = String.format("%s,%s,%d,%d", book.getTitle(), book.getCreator(), book.getYearPublished(), book.getIsbn());
            lines.add(line);
        }

        try {
            Files.write(Paths.get(filename), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Library saved to CSV: " + filename);
        } catch (IOException e) {
            throw e;
        }
    }

    public Set<Book> deserializeFromCSV(String filename) throws IOException {
        Set<Book> books = new TreeSet<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 4) {
                    String title = parts[0];
                    String author = parts[1];
                    int year = Integer.parseInt(parts[2]);
                    long isbn = Long.parseLong(parts[3]);
                    Book book = new Book(title, author, year, isbn);
                    books.add(book);
                }
            }
            System.out.println("Library loaded from CSV: " + filename);
        } catch (IOException | NumberFormatException e) {
            throw e;
        }

        return books;
    }
}
