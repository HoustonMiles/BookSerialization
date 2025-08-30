package com.example;

import java.util.*;
//import com.example.*;

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

        for (Book b : loadedLibrary) {
            if (b.getTitle().equals("Dune")) {
                // Change something to break equality
                b.setAuthor("Fake Author");
                break;
            }
        }

        boolean areEqual = library.equals(loadedLibrary);
        if (areEqual) {
            System.out.println("Books are the same!");
        } else {
            System.out.println("Books are not the same!");
        }
    }
}