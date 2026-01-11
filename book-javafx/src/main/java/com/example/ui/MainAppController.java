package com.example.ui;

import com.example.Book;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainAppController {
    @FXML private BorderPane borderPane;

    private Parent homeLoader, bookLoader, libraryLoader;
    private ObservableList<Book> bookList = FXCollections.observableArrayList();

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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}