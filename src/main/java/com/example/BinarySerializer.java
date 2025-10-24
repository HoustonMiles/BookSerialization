package com.example;
import java.io.*;

public class BinarySerializer {
    public static void binarySerialize(Object obj, String file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(obj);
        }
    }

    public static Object binaryDeserialize(String file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return in.readObject();
        }
    }
}
