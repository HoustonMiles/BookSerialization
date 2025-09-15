package com.example;

import org.junit.jupiter.api.Test;

import java.util.TreeSet;

public class BookTest {
    private BookUtils bookUtils = new BookUtils();

    @Test
    public void testSerializeAndDeserialize() {
        // Create a TreeSet of Book objects
        TreeSet<Media> originalBooks = new TreeSet<>();
        originalBooks.add(new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925, 123456789));
        originalBooks.add(new Book("1984", "George Orwell", 1949, 987654321));

        // Serialize the original books to CSV
        bookUtils.serialize(originalBooks);

        // Deserialize the original books from CSV
        TreeSet<Media> deserializedBooks = bookUtils.deserialize();

         // Check if each original book is equal to the corresponding deserialized book using .equals
        boolean found = false;
        for (Media original : originalBooks) {
            for (Media deserialized : deserializedBooks) {
                if (original.equals(deserialized)) {
                    found = true;
                    break;
                }
            }
        }

        if (found) {
            System.out.println("The deserialized books are the same as the original book: " + originalBooks);
        } else {
            System.out.println("The deserialized books are not the same as the original book: " + originalBooks);
        }
    }
}