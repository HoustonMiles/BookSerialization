package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {
    private BookUtils bookUtils;
    private TreeSet<Media> originalBooks = new TreeSet<>();

    @BeforeEach
    void setUp() {
        bookUtils = new BookUtils();
        originalBooks.add(new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925, 123456789));
        originalBooks.add(new Book("1984", "George Orwell", 1949, 987654321));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void serializeBook() {
        // Serialize the original books to CSV
        bookUtils.serialize(originalBooks);

        for (Media book : originalBooks) {
            if (book.getTitle().equals("1984")) {
                book.setTitle("Animal Farm");
            }
        }

        // Deserialize the original books from CSV
        TreeSet<Media> deserializedBooks = bookUtils.deserialize();

        // Assert that the original and deserialized sets are equal
        assertFalse(originalBooks.equals(deserializedBooks), "The deserialized books should NOT be equal to the original books.");
    }
}