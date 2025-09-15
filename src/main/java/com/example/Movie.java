package com.example;

import java.util.Objects;

public class Movie implements Media {
    private String director;
    private String title;
    private int yearPublished;
    private int runtime;

    public Movie(String title, String director, int yearPublished, int runtime) {
        this.title = title;
        this.director = director;
        this.yearPublished = yearPublished;
        this.runtime = runtime;
    }

    // Getters
    public String getCreator() { return director; }
    public String getTitle() { return title; }
    public int getYearPublished() { return yearPublished; }
    public int getRuntime() { return runtime; }

    // Setters
    public void setCreator(String director) { this.director = director; }
    public void setTitle(String title) { this.title = title; }
    public void setYearPublished(int yearPublished) { this.yearPublished = yearPublished; }
    public void setRuntime(int runtime) { this.runtime = runtime; }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        Movie movie = (Movie) o;
        return title.equals(movie.getTitle()) && director.equals(movie.getCreator()) && yearPublished == movie.getYearPublished() && runtime == movie.getRuntime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, director, yearPublished, runtime);
    }
}
