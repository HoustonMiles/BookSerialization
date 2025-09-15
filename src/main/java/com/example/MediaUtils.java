package com.example;

import java.util.TreeSet;

public interface MediaUtils {
    void serialize(TreeSet<Media> mediaItems);

    TreeSet<Media> deserialize();
}
