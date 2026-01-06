package com.example.ui;

import com.example.Book;
import com.example.BookUtils;
import com.example.BinarySerializer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

public class MainAppController {
    @FXML private BorderPane borderPane;

    private Parent homeLoader, bookLoader, libraryLoader;
    private ObservableList<Book> bookList = FXCollections.observableArrayList();
    private Window primaryStage;

    @FXML
    private void initialize() {
        try {
            // Load Home page
            FXMLLoader homeLoaderFXML = new FXMLLoader(getClass().getResource("Home.fxml"));
            homeLoader = homeLoaderFXML.load();

            // Load Book page
            FXMLLoader bookLoaderFXML = new FXMLLoader(getClass().getResource("Book.fxml"));
            bookLoader = bookLoaderFXML.load();
            BookController bookController = bookLoaderFXML.getController();
            bookController.setBookList(bookList);

            // Load Library page
            FXMLLoader libraryLoaderFXML = new FXMLLoader(getClass().getResource("Library.fxml"));
            libraryLoader = libraryLoaderFXML.load();
            LibraryController libraryController = libraryLoaderFXML.getController();
            libraryController.setBookList(bookList);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start with welcome message or empty center
        goToHome();
    }

    @FXML
    private void goToHome() {
        borderPane.setCenter(homeLoader);
    }

    @FXML
    private void goToBook() {
        borderPane.setCenter(bookLoader);
    }

    @FXML
    private void goToLibrary() {
        borderPane.setCenter(libraryLoader);
    }

    @FXML
    private void handleSaveButtonAction() {
        try {
            if (bookList.isEmpty()) {
                showAlert("Failure", "Table is empty");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Serialization File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Serialization Files", "*.xml", "*.csv", "*.bin")
            );
            fileChooser.setInitialFileName("serializedBooks");
            File selectedFile = fileChooser.showSaveDialog(primaryStage);

            if (selectedFile != null) {
                TreeSet<Book> bookSet = new TreeSet<>(bookList);
                if (selectedFile.getName().endsWith(".csv")) {
                    BookUtils.serializeToCSV(bookSet, selectedFile);
                } else if (selectedFile.getName().endsWith(".xml")) {
                    BookUtils.serializeToXML(bookSet, selectedFile);
                } else if (selectedFile.getName().endsWith(".bin")) {
                    BinarySerializer.binarySerialize(bookSet, selectedFile);
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
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Serialization Files", "*.xml", "*.csv", "*.bin")
            );
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                Set<Book> deserializedBooks = null;

                if (selectedFile.getName().endsWith(".csv")) {
                    deserializedBooks = BookUtils.deserializeFromCSV(selectedFile);
                } else if (selectedFile.getName().endsWith(".xml")) {
                    deserializedBooks = BookUtils.deserializeFromXML(selectedFile);
                } else if (selectedFile.getName().endsWith(".bin")) {
                    deserializedBooks = (Set<Book>) BinarySerializer.binaryDeserialize(selectedFile);
                    if (deserializedBooks == null) {
                        deserializedBooks = new TreeSet<>();
                    }
                }

                if (deserializedBooks != null && !deserializedBooks.isEmpty()) {
                    bookList.addAll(deserializedBooks);
                } else {
                    showAlert("Failure", "File is empty");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            showAlert("Error", "Error loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}