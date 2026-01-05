package com.example.ui;

import com.example.Book;
import com.example.BookUtils;
import com.example.BinarySerializer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import static jdk.jfr.consumer.EventStream.openFile;

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
    @FXML private Button editButton;
    @FXML private Button saveFileButton;
    @FXML private Button loadFileButton;
    @FXML private MenuButton sortListButton;

    @FXML private Label statusLabel;

    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private SortedList<Book> sortedList = new SortedList<>(bookList);

    private Window primaryStage;

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("author"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<Book, Integer>("year"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<Book, String>("isbn"));

        bookTable.setItems(sortedList);

        MenuItem authorItem = new MenuItem("Author Ordered");
        MenuItem titleItem = new MenuItem("Title Ordered");
        MenuItem yearItem = new MenuItem("Year Ordered");
        MenuItem isbnItem = new MenuItem("ISBN Ordered");

        authorItem.setOnAction(e -> sortedList.setComparator(Book.BY_AUTHOR));
        titleItem.setOnAction(e -> sortedList.setComparator(Book.BY_TITLE));
        yearItem.setOnAction(e -> sortedList.setComparator(Book.BY_YEAR));
        isbnItem.setOnAction(e -> sortedList.setComparator(Book.BY_ISBN));

        sortListButton.getItems().addAll(authorItem, titleItem, yearItem, isbnItem);

        statusLabel.setText("Ready");
    }

    @FXML
    private void handleAddButtonAction() {
        try {
            String title = titleField.getText();
            String author = authorField.getText();
            Integer year = Integer.parseInt(yearField.getText());
            String isbn = isbnField.getText().trim();

            if (title.isEmpty() || author.isEmpty() || year == null || isbn.isEmpty()) {
                statusLabel.setText("Please fill all the fields");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Please fill all the fields");
                alert.showAndWait();
                return;
            }

            Book newBook = new Book(title, author, year, isbn);
            if (!duplicateBook(newBook)) {
                bookList.add(newBook);

                titleField.clear();
                authorField.clear();
                yearField.clear();
                isbnField.clear();

                statusLabel.setText("Book added");
            } else {
                statusLabel.setText("Book already exists");
            }
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Failure");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all the fields");
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private boolean duplicateBook(Book book) {
        for (int i = 0; i < bookList.size(); i++) {
            if (book.equals(bookList.get(i))) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void handleRemoveButtonAction() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Failure");
            alert.setHeaderText(null);
            alert.setContentText("Please select a book");
            alert.showAndWait();
        } else {
            bookList.remove(selectedBook);
            statusLabel.setText("Book removed!");
        }
    }

    @FXML
    private void handleEditButtonAction() {
        Book selectedBook = bookTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Failure");
            alert.setHeaderText(null);
            alert.setContentText("Please select a book");
            alert.showAndWait();
        } else {
            bookTable.getSelectionModel().select(selectedBook);

            titleField.setText(selectedBook.getTitle());
            authorField.setText(selectedBook.getAuthor());
            yearField.setText(String.valueOf(selectedBook.getYear()));
            isbnField.setText(selectedBook.getIsbn());

            statusLabel.setText("Book being edited!");
        }
    }

    @FXML
    private void handleSaveButtonAction() {
        try {
            if (bookList.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Failure");
                alert.setHeaderText(null);
                alert.setContentText("Table is empty");
                alert.showAndWait();
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Serialization File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialization Files", "*.xml", "*.csv", "*.bin"));
            fileChooser.setInitialFileName("serializedBooks");
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            TreeSet<Book> bookSet = new TreeSet<>(bookList);
            if (selectedFile != null) {
                if (selectedFile.getName().endsWith(".csv")) {
                    BookUtils.serializeToCSV(bookSet, selectedFile);
                } else if (selectedFile.getName().endsWith(".xml")) {
                    BookUtils.serializeToXML(bookSet, selectedFile);
                } else if (selectedFile.getName().endsWith(".bin")) {
                    BinarySerializer.binarySerialize(bookSet, selectedFile);
                } else {

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadButtonAction() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Serialization File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialization Files", "*.xml", "*.csv", "*.bin"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            Set<Book> deserializedToBooks = null;
            if (selectedFile != null) {
                if (selectedFile.getName().endsWith(".csv")) {
                    deserializedToBooks = BookUtils.deserializeFromCSV(selectedFile);

                } else if (selectedFile.getName().endsWith(".xml")) {
                    deserializedToBooks = BookUtils.deserializeFromXML(selectedFile);

                } else if (selectedFile.getName().endsWith(".bin")) {
                    deserializedToBooks = (Set<Book>) BinarySerializer.binaryDeserialize(selectedFile);
                    if (deserializedToBooks == null) {
                        deserializedToBooks = new TreeSet<>();
                    }

                } else {

                }
            }
            if (deserializedToBooks.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Failure");
                alert.setHeaderText(null);
                alert.setContentText("File is empty");
                alert.showAndWait();
                return;
            }
            for (Book book : deserializedToBooks) {
                if (!duplicateBook(book)) {
                    bookList.add(book);
                } else {
                    statusLabel.setText("Book already exists");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Error loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
