package com.example.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainAppController {
    @FXML private BorderPane borderPane;

    private Parent homeLoader, bookLoader, libraryLoader;

    @FXML
    private void initialize() {
        try {
            // Load Home page
            FXMLLoader homeLoaderFXML = new FXMLLoader(getClass().getResource("Home.fxml"));
            homeLoader = homeLoaderFXML.load();

            // Load Library page first — it owns the tab data and the addBook() logic.
            // FXMLLoader also loads the embedded Book.fxml (via <fx:include>) and
            // wires bookFormController automatically inside LibraryController.initialize().
            FXMLLoader libraryLoaderFXML = new FXMLLoader(getClass().getResource("Library.fxml"));
            libraryLoader = libraryLoaderFXML.load();
            LibraryController libraryController = libraryLoaderFXML.getController();

            // Load Book page as a standalone view too (for the "Add Book" nav button).
            // We manually give it the same LibraryController so both views share one source of truth.
            FXMLLoader bookLoaderFXML = new FXMLLoader(getClass().getResource("Book.fxml"));
            bookLoader = bookLoaderFXML.load();
            BookController bookController = bookLoaderFXML.getController();
            bookController.setLibraryController(libraryController);

        } catch (IOException e) {
            e.printStackTrace();
        }

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