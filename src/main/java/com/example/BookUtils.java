package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class BookUtils {
    public static void serializeToCSV(Set<Book> books, String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("Title,Author,YearPublished,ISBN"); // CSV header

        for (Book book: books) { // Skip header
            String line = String.format("%s,%s,%d,%s", book.getTitle(), book.getCreator(), book.getYearPublished(), book.getIsbn());
            lines.add(line);
        }

        try {
            Files.write(Paths.get(filename), lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Library saved to CSV: " + filename);
        } catch (IOException e) {
            throw e;
        }
    }

    public static Set<Book> deserializeFromCSV(String filename) throws IOException {
        Set<Book> books = new TreeSet<>();

        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (int i = 1; i < lines.size(); i++) { // Skip header
                String[] parts = lines.get(i).split(",");
                if (parts.length >= 4) {
                    String title = parts[0];
                    String author = parts[1];
                    int year = Integer.parseInt(parts[2]);
                    String isbn = parts[3];
                    Book book = new Book(title, author, year, isbn);
                    books.add(book);
                }
            }
            System.out.println("Library loaded from CSV: " + filename);
        } catch (IOException | NumberFormatException e) {
            throw e;
        }

        return books;
    }

    public static void serializeToXML(Set<Book> books, String filename) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc = builder.newDocument();

        Element root = doc.createElement("books");
        doc.appendChild(root);

        // Parser
        for (Book book: books) {
            Element bookElement = doc.createElement("book");
            bookElement.setAttribute("title", book.getTitle());
            bookElement.setAttribute("author", book.getCreator());
            bookElement.setAttribute("yearPublished", String.valueOf(book.getYearPublished()));
            bookElement.setAttribute("isbn", book.getIsbn());
            root.appendChild(bookElement);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filename));
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<Book> deserializeFromXML(String filename) throws IOException {
        Set<Book> books = new TreeSet<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        Document doc;
        try {
            doc = builder.parse(new File(filename));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }

        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("book");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                String title = element.getAttribute("title");
                String author = element.getAttribute("author");
                int year = Integer.parseInt(element.getAttribute("yearPublished"));
                String isbn = element.getAttribute("isbn");
                books.add(new Book(title, author, year, isbn));
            }
        }
        return books;
    }
}
