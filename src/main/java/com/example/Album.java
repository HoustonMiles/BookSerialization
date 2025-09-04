package com.example;

import java.util.Objects;

public class Album extends Media{
    private String artist;
    private int runtime;

    public Album(String title, String artist, int yearPublished, int runtime){
        super(title, yearPublished);
        this.artist = artist;
        this.runtime = runtime;
    }

    @Override
    public String getCreator() {
        return artist;
    }
    // Getters
    public String getArtist() { return artist; }
    public int getRuntime() { return runtime; }
    // Setters
    public void setArtist(String artist) { this.artist = artist; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        Album album = (Album) o;
        return artist.equals(album.artist) && runtime == album.runtime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), artist, runtime);
    }
}
