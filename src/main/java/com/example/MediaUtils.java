package com.example;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class MediaUtils {

    public static void serialize(Set<Media> mediaItems, String filename) {
        List<String> lines = new ArrayList<>();
        lines.add("Type,Title,Author,YearPublished,Extra"); // CSV header

        for (Media item : mediaItems) {
            String type = item.getClass().getSimpleName();
            String title = escape(item.getTitle());
            String author = escape(item.getCreator());
            String year = String.valueOf(item.getYearPublished());
            String extra = "";

            if (item instanceof Movie movie) {
                extra = String.valueOf(movie.getRuntime());
            } else if (item instanceof Album album) {
                extra = String.valueOf(album.getRuntime());
            }

            lines.add(String.join(",", type, title, author, year, escape(extra)));
        }

        try {
            Files.write(Paths.get(filename), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Library saved to CSV: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Set<Media> deserialize(String filename) {
        Set<Media> mediaItems = new HashSet<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length >= 5) {
                    String type = unescape(parts[0]);
                    String title = unescape(parts[1]);
                    String author = unescape(parts[2]);
                    int year = Integer.parseInt(parts[3]);
                    String extra = unescape(parts[4]);

                    switch (type) {
                        case "Book" -> mediaItems.add(new Book(title, author, year));
                        case "Movie" -> mediaItems.add(new Movie(title, author, year, Integer.parseInt(extra)));
                        case "Album" -> mediaItems.add(new Album(title, author, year, Integer.parseInt(extra)));
                        default -> System.err.println("Unknown media type: " + type);
                    }
                }
            }
            System.out.println("Library loaded from CSV: " + filename);
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return mediaItems;
    }

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
