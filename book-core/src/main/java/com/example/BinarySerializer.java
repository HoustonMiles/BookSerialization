package com.example;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BinarySerializer {
    public static void binarySerialize(Object obj, String filename) {
        ObjectOutputStream oos = null;
        try {
            Path path = Paths.get("..", "logs", filename);
            oos = new ObjectOutputStream(new FileOutputStream(path.toFile()));
            System.out.println("Library saved to binary file: " + filename);
            oos.writeObject(obj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (oos != null) {
                    oos.flush();
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Object binaryDeserialize(String filename) throws ClassNotFoundException {
        try {
            Path path = Paths.get("..", "logs", filename);
            FileInputStream fis = new FileInputStream(path.toFile());
            ObjectInputStream ois = new ObjectInputStream(fis);
            System.out.println("Library loaded from binary file: " + filename);
            return ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
