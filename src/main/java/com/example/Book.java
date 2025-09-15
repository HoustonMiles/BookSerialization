package com.example;

import java.util.Objects;
import java.util.TreeSet;

public class Book implements Media {

    private String author;
    private String title;
    private int yearPublished;
    private int isbn;

    public Book(String title, String author, int yearPublished, int isbn) {
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

    public int getIsbn() {
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

    public void setIsbn(int isbn) {
        this.isbn = isbn;
    }

    @Override
    public boolean equals(Object o) {
        boolean bool = false;
        if (!super.equals(o))
            return false;
        TreeSet set = (TreeSet) o;
        for (Object obj: set) {
            Book book = (Book) obj;
            if (title.equals(book.getTitle()) && author.equals(book.getCreator()) && yearPublished == book.getYearPublished() && isbn == book.getIsbn()) {
                bool = true;
            }
        }
        return bool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author, yearPublished, isbn);
    }
}
