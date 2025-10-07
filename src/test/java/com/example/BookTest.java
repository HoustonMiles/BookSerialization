package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {
    private final Set<Book> originalBooks = new TreeSet<>();

    @BeforeEach
    void setUp() {
        Book book3 = new Book("The Way of Kings", "Brandon Sanderson", 2010, 9781427228154L);
        Book book1 = new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925, 9780743273565L);
        Book book2 = new Book("1984", "George Orwell", 1949, 9780451524935L);

        originalBooks.addAll(Arrays.asList(book1, book2, book3));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void serializeBook() throws IOException {
        String filename = "books.csv";

        // Serialize the original books to CSV
        BookUtils.serializeToCSV(originalBooks, filename);

        System.out.println("Test");
        // Deserialize the original books from CSV
        Set<Book> deserializedBooks = BookUtils.deserializeFromCSV(filename);

        // Assert that the original and deserialized sets are equal
        assertEquals(originalBooks, deserializedBooks, "The deserialized books should be equal to the original books.");
    }
}