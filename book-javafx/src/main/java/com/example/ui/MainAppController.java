package com.example.ui;

import com.example.Book;
import com.example.BookUtils;
import com.example.BinarySerializer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class MainAppController {
    @FXML private TableView<Book> bookTable;

    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, Integer> yearColumn;
    @FXML private TableColumn<Book, String> isbnColumn;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;

    @FXML private Button addButton;
    @FXML private Button removeButton;
    @FXML private Button saveCSVButton;
    @FXML private Button loadCSVButton;
    @FXML private Button saveXMLButton;
    @FXML private Button loadXMLButton;
    @FXML private Button saveBinaryButton;
    @FXML private Button loadBinaryButton;

    @FXML private Label statusLabel;

    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    String csvFile = "book.csv";
    String xmlFile = "book.xml";
    String binaryFile = "book.bin";

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<Book, Integer>("year"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("isbn"));

        bookTable.setItems(bookList);

        statusLabel.setText("Ready");
    }

    @FXML
    private void handleAddButtonAction() {
        try {
            String title = titleField.getText();
            String author = authorField.getText();
            Integer year = Integer.parseInt(yearField.getText());
            String isbn = isbnField.getText().trim();

            if (title.isEmpty() || author.isEmpty() || year == 0 || isbn.isEmpty()) {
                statusLabel.setText("Please fill all the fields");
                return;
            }

            Book newBook = new Book(title, author, year, isbn);
            bookList.add(newBook);

            titleField.clear();
            authorField.clear();
            yearField.clear();
            isbnField.clear();

            statusLabel.setText("Book added!");
        } catch (NumberFormatException e) {
            statusLabel.setText("Error: Year must be a number");
        }
    }

    @FXML
    private void handleRemoveButtonAction() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            statusLabel.setText("No book selected");
        } else {
            bookList.remove(selectedBook);
            statusLabel.setText("Book removed!");
        }
    }

    @FXML
    private void handleSaveCSVButtonAction() {
        try {
            TreeSet<Book> bookSet = new TreeSet<>(bookList);
            BookUtils.serializeToCSV(bookSet, csvFile);
            statusLabel.setText("CSV serialized!");
        } catch (IOException e) {
            statusLabel.setText("Error saving to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadCSVButtonAction() throws IOException {
        try {
            Set<Book> deserializedCSVToBooks = BookUtils.deserializeFromCSV(csvFile);
            bookList.clear();
            bookList.addAll(deserializedCSVToBooks);
            statusLabel.setText("CSV deserialized!");
        } catch (IOException e) {
            statusLabel.setText("Error loading from CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveXMLButtonAction() throws IOException {
        try {
            TreeSet<Book> bookSet = new TreeSet<>(bookList);
            BookUtils.serializeToXML(bookSet, xmlFile);
            statusLabel.setText("XML serialized!");
        } catch (IOException e) {
            statusLabel.setText("Error saving to XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadXMLButtonAction() throws IOException {
        try {
            Set<Book> deserializedXMLToBooks = BookUtils.deserializeFromXML(xmlFile);
            bookList.clear();
            bookList.addAll(deserializedXMLToBooks);
            statusLabel.setText("XML deserialized!");
        } catch (IOException e) {
            statusLabel.setText("Error loading from XML: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveBinaryButtonAction() {
        try {
            TreeSet<Book> bookSet = new TreeSet<>(bookList);
            BinarySerializer.binarySerialize(bookSet, binaryFile);
            statusLabel.setText("Binary serialized!");
        } catch (Exception e) {
            statusLabel.setText("Error binary serialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadBinaryButtonAction() throws ClassNotFoundException {
        try {
            Set<Book> loadedBooks = (Set<Book>) BinarySerializer.binaryDeserialize(binaryFile);
            if (loadedBooks != null) {
                bookList.clear();
                bookList.addAll(loadedBooks);
                statusLabel.setText("Binary deserialized!");
            } else {
                statusLabel.setText("Error binary deserialization!");
            }
        } catch (ClassNotFoundException e) {
            statusLabel.setText("Error binary deserialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
