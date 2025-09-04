package com.example;

import java.util.Objects;

public class Book extends Media {
    private String author;

    public Book(String title, String author, int yearPublished) {
        super(title, yearPublished);
        this.author = author;
    }

    // Getters
    @Override
    public String getCreator() {
        return author;
    }

    // Setters
    public void setAuthor(String author) { this.author = author; }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        Book book = (Book) o;
        return author.equals(book.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), author);
    }
}
