package com.example.ui;

import com.example.Book;
import com.example.BookValidator;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class BookController {
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;

    @FXML private Label  statusLabel;
    @FXML private Button validateButton;
    @FXML private Button addButton;

    private LibraryController libraryController;

    // Tracks whether the current form content has been verified via Open Library
    private boolean verified = false;

    public void setLibraryController(LibraryController libraryController) {
        this.libraryController = libraryController;
    }

    // ── Add ───────────────────────────────────────────────────────────────────

    @FXML
    private void handleAddButtonAction() {
        if (libraryController == null) {
            statusLabel.setText("Error: library not connected.");
            return;
        }

        String title   = titleField.getText().trim();
        String author  = authorField.getText().trim();
        String isbn    = isbnField.getText().trim();
        String yearRaw = yearField.getText().trim();

        if (title.isEmpty() || author.isEmpty() || yearRaw.isEmpty() || isbn.isEmpty()) {
            statusLabel.setText("Please fill all the fields");
            showAlert("Please fill all the fields");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearRaw);
        } catch (NumberFormatException e) {
            statusLabel.setText("Year must be a number");
            showAlert("Year must be a valid number");
            return;
        }

        Book newBook = new Book(title, author, year, isbn);
        newBook.setVerified(verified); // stamp the verified state onto the book
        String result = libraryController.addBook(newBook);
        statusLabel.setText(result);

        if ("Book added".equals(result)) {
            clearFields(); // also resets verified = false
        }
    }

    // ── Validate ──────────────────────────────────────────────────────────────

    @FXML
    private void handleValidateAction() {
        String title   = titleField.getText().trim();
        String author  = authorField.getText().trim();
        String isbn    = isbnField.getText().trim();
        String yearRaw = yearField.getText().trim();

        if (title.isEmpty() && isbn.isEmpty()) {
            statusLabel.setText("Enter at least a title or ISBN to validate.");
            return;
        }

        int year = 0;
        try { year = Integer.parseInt(yearRaw); } catch (NumberFormatException ignored) {}

        Book bookToValidate = new Book(title, author, year, isbn);

        setButtonsDisabled(true);
        statusLabel.setText("Validating with Open Library…");

        Task<BookValidator.ValidationResult> task = new Task<>() {
            @Override
            protected BookValidator.ValidationResult call() {
                return BookValidator.validate(bookToValidate);
            }
        };

        task.setOnSucceeded(e -> {
            setButtonsDisabled(false);
            BookValidator.ValidationResult result = task.getValue();
            statusLabel.setText(result.message);

            if (result.found) {
                verified = true; // mark that this book passed validation
                Book c = result.completedBook;
                // Only fill in fields the user left blank
                if (title.isEmpty()   && c.getTitle()  != null) titleField.setText(c.getTitle());
                if (author.isEmpty()  && c.getAuthor() != null) authorField.setText(c.getAuthor());
                if (yearRaw.isEmpty() && c.getYear()   != 0)    yearField.setText(String.valueOf(c.getYear()));
                if (isbn.isEmpty()    && c.getIsbn()   != null) isbnField.setText(c.getIsbn());
            }
        });

        task.setOnFailed(e -> {
            setButtonsDisabled(false);
            statusLabel.setText("Validation failed: " + task.getException().getMessage());
        });

        new Thread(task, "book-validator").start();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void setButtonsDisabled(boolean disabled) {
        if (validateButton != null) validateButton.setDisable(disabled);
        if (addButton      != null) addButton.setDisable(disabled);
    }

    private void clearFields() {
        titleField.clear();
        authorField.clear();
        yearField.clear();
        isbnField.clear();
        verified = false; // reset for the next book
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}