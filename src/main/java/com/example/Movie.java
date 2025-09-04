package com.example;

import java.util.Objects;

public class Movie extends Media {
    private String director;
    private int runtime;

    public Movie(String title, String director, int yearPublished, int runtime) {
        super(title, yearPublished);
        this.director = director;
        this.runtime = runtime;
    }

    @Override
    public String getCreator() {
        return director;
    }
    // Getters
    public String getDirector() {
        return director;
    }
    public int getRuntime() { return runtime; }

    // Setters
    public void setDirector(String author) { this.director = author; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        Movie movie = (Movie) o;
        return director.equals(movie.director) && runtime == movie.runtime;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), director, runtime);
    }
}
