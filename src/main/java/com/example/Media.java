package com.example;

import java.io.Serial;
import java.io.Serializable;

public interface Media extends Serializable {
    @Serial
    long serialVersionUID = 1L;

    String getCreator();

    String getTitle();

    int getYearPublished();

    void setCreator(String creator);

    void setTitle(String title);

    void setYearPublished(int yearPublished);

}