package com.example;

import java.util.*;
//import com.example.*;

public class Driver {
    public static void main(String[] args) {
        Set<Media> library = new HashSet<>();
        library.add(new Movie("Dune: Part 1", "Denis Villeneuve", 2021, 120));
        library.add(new Book("The Hobbit", "J.R.R. Tolkien", 1937));
        library.add(new Book("1984", "George Orwell", 1949));
        library.add(new Album("Wish", "The Cure", 1992, 66));

        // Save to CSV
        MediaUtils.serialize(library, "media.csv");

        // Load from CSV
        Set<Media> loadedLibrary = new HashSet<>(MediaUtils.deserialize("media.csv"));

        //library.clear();

        boolean areEqual = library.equals(loadedLibrary);
        if (areEqual) {
            System.out.println("Media are the same!");
        } else {
            System.out.println("Media are not the same!");
        }
    }
}