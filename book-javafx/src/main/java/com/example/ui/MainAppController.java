package com.example.ui;

import com.example.Book;
import com.example.BookUtils;
import com.example.BinarySerializer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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
    private TreeSet<Book> bookSet = new TreeSet<>();
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
    private void handleSaveCSVButtonAction() throws IOException {
        bookSet = new TreeSet<>(bookList);
        BookUtils.serializeToCSV(bookSet, csvFile);
    }

    @FXML
    private void handleLoadCSVButtonAction() throws IOException {
        Set<Book> deserializedCSVToBooks = BookUtils.deserializeFromCSV(csvFile);
    }

    @FXML
    private void handleSaveXMLButtonAction() throws IOException {
        bookSet = new TreeSet<>(bookList);
        BookUtils.serializeToXML(bookSet, xmlFile);
    }

    @FXML
    private void handleLoadXMLButtonAction() throws IOException {
        Set<Book> deserializedXMLToBooks = BookUtils.deserializeFromXML(xmlFile);
    }

    @FXML
    private void handleSaveBinaryButtonAction() {
        bookSet = new TreeSet<>(bookList);
        BinarySerializer.binarySerialize(bookSet, binaryFile);
    }

    @FXML
    private void handleLoadBinaryButtonAction() throws ClassNotFoundException {
        BinarySerializer.binaryDeserialize(binaryFile);
    }

}
