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
        Book book3 = new Book("The Way of Kings", "Brandon Sanderson", 2010, "9781427228154");
        Book book1 = new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925, "9780743273565");
        Book book2 = new Book("1984", "George Orwell", 1949, "9780451524935");

        originalBooks.addAll(Arrays.asList(book1, book2, book3));
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void serializeBook() throws IOException, ClassNotFoundException {
        String csvFile = "book.csv";
        String xmlFile = "book.xml";
        String binaryFile = "book.ser";

        // Serialize the original books to CSV
        BookUtils.serializeToCSV(originalBooks, csvFile);
        BookUtils.serializeToXML(originalBooks, xmlFile);
        BinarySerializer.binarySerialize(originalBooks, binaryFile);
        for (Book book : originalBooks) {
            System.out.println(book.toString());
        }

        System.out.println("Test");
        // Deserialize the original books from CSV
        Set<Book> deserializedCSVToBooks = BookUtils.deserializeFromCSV(csvFile);
        Set<Book> deserializedXMLToBooks = BookUtils.deserializeFromXML(xmlFile);
        Set<Book> deserializedBinaryToBooks = (Set<Book>) BinarySerializer.binaryDeserialize(binaryFile);
        for (Book book : deserializedXMLToBooks) {
            System.out.println(book.toString());
        }

        // Assert that the original and deserialized sets are equal
        assertEquals(originalBooks, deserializedCSVToBooks, "The deserialized books should be equal to the original books.");
        assertEquals(originalBooks, deserializedXMLToBooks, "The deserialized books should be equal to the original books.");
        assertEquals(originalBooks, deserializedBinaryToBooks, "The deserialized books should be equal to the original books.");
    }
}