package com.example;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class Book implements Media, Comparable<Book> {

    private String author;
    private String title;
    private int yearPublished;
    private long isbn;

    public Book(String title, String author, int yearPublished, long isbn) {
        this.author = author;
        this.title = title;
        this.yearPublished = yearPublished;
        this.isbn = isbn;
    }

    // Getters
    public String getCreator() {
        return this.author;
    }

    public String getTitle() {
        return this.title;
    }

    public int getYearPublished() {
        return this.yearPublished;
    }

    public long getIsbn() {
        return this.isbn;
    }

    // Setters
    public void setCreator(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }

    public void setIsbn(long isbn) {
        this.isbn = isbn;
    }

    public String toString() {
        String output = this.author + "\t" + this.title + "\t" + this.yearPublished + "\t" + this.isbn + "\n";
        return output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(title, book.title) &&
                Objects.equals(author, book.author) &&
                yearPublished == book.yearPublished &&
                isbn == book.isbn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, yearPublished, isbn);
    }

    @Override
    public int compareTo(Book o) {
        int yearCompare = Integer.compare(this.yearPublished, o.yearPublished);
        if (yearCompare != 0) return yearCompare;

        int titleCompare = this.title.compareTo(o.title);
        if (titleCompare != 0) return titleCompare;

        return this.author.compareTo(o.author);
    }
}
