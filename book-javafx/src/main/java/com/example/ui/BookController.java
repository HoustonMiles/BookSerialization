package com.example.ui;

import com.example.Book;
import com.example.ui.BookValidator;

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
    @FXML private Button saveButton;

    private LibraryController libraryController;
    private Book bookToEdit = null;

    // Tracks whether the current form content has been verified via Open Library
    private boolean verified = false;

    public void setLibraryController(LibraryController libraryController) {
        this.libraryController = libraryController;
    }

    @FXML
    private void initialize() {
        // Tooltip on validate button
        Tooltip tooltip = new Tooltip("If you don't have all the info, this button can complete it for you.");
        tooltip.setShowDelay(javafx.util.Duration.millis(100));
        tooltip.setShowDuration(javafx.util.Duration.INDEFINITE);
        validateButton.setTooltip(tooltip);
    }

    @FXML
    private void handleSaveButtonAction() {
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

        if (bookToEdit != null) {
            // Edit mode — update the existing book in place
            bookToEdit.setTitle(title);
            bookToEdit.setAuthor(author);
            bookToEdit.setYear(year);
            bookToEdit.setIsbn(isbn);
            bookToEdit.setVerified(verified);
            libraryController.refreshTable();
            statusLabel.setText("Book updated.");
            bookToEdit = null;
            titleField.getScene().getWindow().hide();
        } else {
            // Add mode — create a new book
            Book newBook = new Book(title, author, year, isbn);
            newBook.setVerified(verified);
            String result = libraryController.addBook(newBook);
            statusLabel.setText(result);
            if ("Book added".equals(result)) clearFields();
        }
    }

    @FXML
    private void handleClearButtonAction() {
        titleField.clear();
        authorField.clear();
        yearField.clear();
        isbnField.clear();
    }

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

    public void setFields(Book book) {
        bookToEdit = book;
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        yearField.setText(String.valueOf(book.getYear()));
        isbnField.setText(book.getIsbn());
        verified = book.isVerified();
    }

    private void setButtonsDisabled(boolean disabled) {
        if (validateButton != null) validateButton.setDisable(disabled);
        if (saveButton      != null) saveButton.setDisable(disabled);
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