package com.example;

import java.util.*;

public class BookDriver {
    public static void main(String[] args) {
        Set<Book> library = new HashSet<>();
        library.add(
                new Book("Dune", "Frank Herbert", 1965)
                //new Book("The Hobbit", "J.R.R. Tolkien", 1937),
                //new Book("1984", "George Orwell", 1949)
        );

        // Save to CSV
        BookUtils.serialize(library, "books.csv");

        // Load from CSV
        Set<Book> loadedLibrary = new HashSet<>(BookUtils.deserialize("books.csv"));

        boolean areEqual = library.equals(loadedLibrary);
        if (areEqual) {
            System.out.println("Books are the same!");
        } else {
            System.out.println("Books are not the same!");
        }
    }
}