package com.example;
import java.io.*;

public class BinarySerializer {
    public static void binarySerialize(Object obj, File filename) {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(filename));
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

    public static Object binaryDeserialize(File filename) throws ClassNotFoundException {
        try {
            FileInputStream fis = new FileInputStream(filename);
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
