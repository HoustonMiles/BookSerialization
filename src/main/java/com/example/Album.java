package com.example;

import java.util.Objects;

public class Album implements Media{
    private String artist;
    private String title;
    private int yearPublished;
    private int runtime;

    public Album(String title, String artist, int yearPublished, int runtime){
        this.artist = artist;
        this.title = title;
        this.yearPublished = yearPublished;
        this.runtime = runtime;
    }

    // Getters
    public String getCreator() {
        return this.artist;
    }

    public String getTitle() {
        return this.title;
    }

    public int getYearPublished() {
        return this.yearPublished;
    }

    public int getRuntime() {
        return this.runtime;
    }

    // Setters
    public void setCreator(String artist) { this.artist = artist; }
    public void setTitle(String title) { this.title = title; }
    public void setYearPublished(int yearPublished) { this.yearPublished = yearPublished; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        Album album = (Album) o;
        return title.equals(album.getTitle()) && artist.equals(album.getCreator()) && yearPublished == album.getYearPublished() && runtime == album.getRuntime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, artist, yearPublished, runtime);
    }
}
