package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class BookUtils implements MediaUtils{
    public static String filename = "books.csv";
    public void serialize(TreeSet<Media> mediaItems) {
        List<String> lines = new ArrayList<>();
        lines.add("Title,Author,YearPublished,ISBN"); // CSV header

        for (Media item: mediaItems) { // Skip header
            Book book = (Book) item;
            String line = String.format("%s,%s,%d,%d", book.getTitle(), book.getCreator(), book.getYearPublished(), book.getIsbn());
            lines.add(line);
        }

        try {
            Files.write(Paths.get(filename), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Library saved to CSV: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TreeSet<Media> deserialize() {
        TreeSet<Media> mediaItems = new TreeSet<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 5) {
                    String title = parts[1];
                    String author = parts[3];
                    int year = Integer.parseInt(parts[3]);
                    int isbn = Integer.parseInt(parts[4]);
                    Book book = new Book(title, author, year, isbn);
                    mediaItems.add(book);
                }
            }
            System.out.println("Library loaded from CSV: " + filename);
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return mediaItems;
    }
}
