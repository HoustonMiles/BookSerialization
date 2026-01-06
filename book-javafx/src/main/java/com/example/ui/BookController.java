package com.example.ui;

import com.example.Book;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class BookController {
    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;

    @FXML private Label statusLabel;

    private ObservableList<Book> bookList;

    public void setBookList(ObservableList<Book> bookList) {
        this.bookList = bookList;
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
}