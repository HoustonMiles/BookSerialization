package com.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

public class Book implements Serializable, Comparable<Book> {
    @Serial
    long serialVersionUID = 1L;

    private String author;
    private String title;
    private int year;
    private String isbn;

    public Book(String title, String author, int year, String isbn) {
        this.author = author;
        this.title = title;
        this.year = year;
        this.isbn = isbn;
    }

    // Getters
    public String getAuthor() {
        return this.author;
    }

    public String getTitle() {
        return this.title;
    }

    public int getYear() {
        return this.year;
    }

    public String getIsbn() {
        return this.isbn;
    }

    // Setters
    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String toString() {
        String output = this.author + ",\t" + this.title + ",\t" + this.year + ",\t" + this.isbn;
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(title, book.title) &&
                Objects.equals(author, book.author) &&
                year == book.year &&
                Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, year, isbn);
    }

    @Override
    public int compareTo(Book o) {
        int result = Integer.compare(this.year, o.year);
        if (result != 0) return result;

        result = this.title.compareTo(o.title);
        if (result != 0) return result;

        result = this.author.compareTo(o.author);
        if (result != 0) return result;

        return this.isbn.compareTo(o.isbn);
    }

    public static final Comparator<Book> BY_AUTHOR = Comparator.comparing(Book::getAuthor);
    public static final Comparator<Book> BY_TITLE = Comparator.comparing(Book::getTitle);
    public static final Comparator<Book> BY_YEAR = Comparator.comparing(Book::getYear);
    public static final Comparator<Book> BY_ISBN = Comparator.comparing(Book::getIsbn);
}
