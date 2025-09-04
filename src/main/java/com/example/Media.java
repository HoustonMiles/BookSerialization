package com.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public abstract class Media implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String title;
    private int yearPublished;

    public Media(String title, int yearPublished) {
        this.title = title;
        this.yearPublished = yearPublished;
    }

    public abstract String getCreator();

    public String getTitle() {
        return title;
    }

    public int getYearPublished() {
        return yearPublished;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setYearPublished(int yearPublished) {
        this.yearPublished = yearPublished;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Media media = (Media) o;
        return yearPublished == media.yearPublished && Objects.equals(title, media.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, yearPublished);
    }
}