package com.example.ui;

import com.example.Book;
import com.example.BookUtils;
import com.example.BinarySerializer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class LibraryController {
    @FXML private TableView<Book> bookTable;

    @FXML private TableColumn<Book, String> titleColumn;
    @FXML private TableColumn<Book, String> authorColumn;
    @FXML private TableColumn<Book, Integer> yearColumn;
    @FXML private TableColumn<Book, String> isbnColumn;

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private TextField isbnField;

    @FXML private TabPane libraryTabPane;

    @FXML private TextField searchField;
    @FXML private ChoiceBox<String> searchChoiceBox;

    @FXML private MenuButton sortListButton;
    @FXML private Label statusLabel;

    private static class LibraryTabData {
        final ObservableList<Book> bookList;
        final FilteredList<Book>   filteredList;
        final SortedList<Book>     sortedList;
        final TableView<Book>      tableView;

        LibraryTabData(TableView<Book> tableView) {
            this.tableView = tableView;
            this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // ← add this
            this.bookList = FXCollections.observableArrayList();
            this.filteredList = new FilteredList<>(bookList, p -> true);
            this.sortedList = new SortedList<>(filteredList);
            tableView.setItems(sortedList);
        }
    }

    private final Map<Tab, LibraryTabData> tabDataMap = new HashMap<>();
    private int tabCounter = 2; // first tab is "Library 1"

    private LibraryTabData currentData() {
        Tab selected = libraryTabPane.getSelectionModel().getSelectedItem();
        return selected == null ? null : tabDataMap.get(selected);
    }

    @FXML
    private void initialize() {
        // Make the FXML table editable
        bookTable.setEditable(true);

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        titleColumn.setOnEditCommit((TableColumn.CellEditEvent<Book, String> t) -> {
            Book book = t.getTableView().getItems().get(t.getTablePosition().getRow());
            book.setTitle(t.getNewValue());
        });

        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        authorColumn.setOnEditCommit((TableColumn.CellEditEvent<Book, String> t) -> {
            Book book = t.getTableView().getItems().get(t.getTablePosition().getRow());
            book.setAuthor(t.getNewValue());
        });

        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        yearColumn.setOnEditCommit((TableColumn.CellEditEvent<Book, Integer> t) -> {
            Book book = t.getTableView().getItems().get(t.getTablePosition().getRow());
            book.setYear(t.getNewValue());
        });

        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        isbnColumn.setOnEditCommit((TableColumn.CellEditEvent<Book, String> t) -> {
            Book book = t.getTableView().getItems().get(t.getTablePosition().getRow());
            book.setIsbn(t.getNewValue());
        });

        Tab firstTab = libraryTabPane.getTabs().get(0);
        firstTab.setText("Library 1");
        tabDataMap.put(firstTab, new LibraryTabData(bookTable));

        MenuItem authorItem = new MenuItem("Author Ordered");
        MenuItem titleItem  = new MenuItem("Title Ordered");
        MenuItem yearItem   = new MenuItem("Year Ordered");
        MenuItem isbnItem   = new MenuItem("ISBN Ordered");

        searchChoiceBox.getItems().addAll("Search Author", "Search Title", "Search Year", "Search ISBN");
        searchChoiceBox.setValue("Search Author");
        searchField.setText("Search Here!");

        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            LibraryTabData data = currentData(); // ← was: if (filteredList != null)
            if (data != null) {
                switch (searchChoiceBox.getValue()) {
                    case "Search Author":
                        data.filteredList.setPredicate(p -> p.getAuthor().toLowerCase().contains(newValue.toLowerCase().trim()));
                        break;
                    case "Search Title":
                        data.filteredList.setPredicate(p -> p.getTitle().toLowerCase().contains(newValue.toLowerCase().trim()));
                        break;
                    case "Search Year":
                        data.filteredList.setPredicate(p -> String.valueOf(p.getYear()).contains(newValue.trim()));
                        break;
                    case "Search ISBN":
                        data.filteredList.setPredicate(p -> p.getIsbn().toLowerCase().contains(newValue.toLowerCase().trim()));
                        break;
                }
            }
        });

        searchChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) searchField.setText("");
        });

        authorItem.setOnAction(e -> { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_AUTHOR); });
        titleItem .setOnAction(e -> { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_TITLE); });
        yearItem  .setOnAction(e -> { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_YEAR); });
        isbnItem  .setOnAction(e -> { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_ISBN); });

        sortListButton.getItems().addAll(authorItem, titleItem, yearItem, isbnItem);
        statusLabel.setText("Ready");
    }

    @FXML
    private void handleNewTabAction() {
        TextInputDialog dialog = new TextInputDialog("Library " + tabCounter);
        dialog.setTitle("New Library");
        dialog.setHeaderText(null);
        dialog.setContentText("Library name:");
        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) return;

            // Clone the column setup onto a fresh TableView
            TableView<Book> newTable = new TableView<>();
            newTable.setEditable(true);
            newTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            newTable.setPrefWidth(680);
            newTable.setPrefHeight(431);

            TableColumn<Book, String>  tc = new TableColumn<>("Title");
            TableColumn<Book, String>  ac = new TableColumn<>("Author");
            TableColumn<Book, Integer> yc = new TableColumn<>("Year");
            TableColumn<Book, String>  ic = new TableColumn<>("ISBN");

            tc.setCellValueFactory(new PropertyValueFactory<>("title"));
            ac.setCellValueFactory(new PropertyValueFactory<>("author"));
            yc.setCellValueFactory(new PropertyValueFactory<>("year"));
            ic.setCellValueFactory(new PropertyValueFactory<>("isbn"));

            tc.setCellFactory(TextFieldTableCell.forTableColumn());
            ac.setCellFactory(TextFieldTableCell.forTableColumn());
            yc.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
            ic.setCellFactory(TextFieldTableCell.forTableColumn());

            tc.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setTitle(t.getNewValue()));
            ac.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setAuthor(t.getNewValue()));
            yc.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setYear(t.getNewValue()));
            ic.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setIsbn(t.getNewValue()));

            newTable.getColumns().addAll(tc, ac, yc, ic);
            VBox tabContent = new VBox(newTable);
            VBox.setVgrow(newTable, javafx.scene.layout.Priority.ALWAYS);
            tabContent.setPrefSize(680, 431);

            Tab tab = new Tab(name.trim(), tabContent);
            tab.setClosable(false);
            tabDataMap.put(tab, new LibraryTabData(newTable));
            libraryTabPane.getTabs().add(tab);
            libraryTabPane.getSelectionModel().select(tab);
            tabCounter++;
            statusLabel.setText("Created library: " + name.trim());
        });
    }

    @FXML
    private void handleDeleteTabAction() {
        if (libraryTabPane.getTabs().size() <= 1) {
            showInfo("Cannot delete the last library.");
            return;
        }
        Tab selected = libraryTabPane.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete library \"" + selected.getText() + "\" and all its books?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                tabDataMap.remove(selected);
                libraryTabPane.getTabs().remove(selected);
                statusLabel.setText("Library deleted.");
            }
        });
    }

    private boolean duplicateBook(Book book) {
        LibraryTabData data = currentData();
        if (data == null) return false;
        for (Book b : data.bookList) {
            if (book.equals(b)) return true;
        }
        return false;
    }

    @FXML
    private void handleRemoveButtonAction() {
        LibraryTabData data = currentData(); // ← was: bookTable / bookList
        if (data == null) return;
        Book selectedBook = data.tableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showInfo("Please select a book.");
        } else {
            data.bookList.remove(selectedBook);
            statusLabel.setText("Book removed!");
        }
    }

    @FXML
    private void handleSaveButtonAction() {
        try {
            LibraryTabData data = currentData(); // ← was: bookList
            if (data == null) return;
            if (data.bookList.isEmpty()) {
                showInfo("Table is empty");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Serialization File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialization Files", "*.xml", "*.csv", "*.bin"));
            fileChooser.setInitialFileName("serializedBooks");
            File selectedFile = fileChooser.showSaveDialog(statusLabel.getScene().getWindow());
            TreeSet<Book> bookSet = new TreeSet<>(data.bookList); // ← was: bookList
            if (selectedFile != null) {
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
            LibraryTabData data = currentData(); // ← was: bookList
            if (data == null) return;
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Serialization File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialization Files", "*.xml", "*.csv", "*.bin"));
            File selectedFile = fileChooser.showOpenDialog(statusLabel.getScene().getWindow());
            Set<Book> deserializedToBooks = null;
            if (selectedFile != null) {
                if (selectedFile.getName().endsWith(".csv")) {
                    deserializedToBooks = BookUtils.deserializeFromCSV(selectedFile);
                } else if (selectedFile.getName().endsWith(".xml")) {
                    deserializedToBooks = BookUtils.deserializeFromXML(selectedFile);
                } else if (selectedFile.getName().endsWith(".bin")) {
                    deserializedToBooks = (Set<Book>) BinarySerializer.binaryDeserialize(selectedFile);
                    if (deserializedToBooks == null) deserializedToBooks = new TreeSet<>();
                }
            }
            if (deserializedToBooks == null || deserializedToBooks.isEmpty()) {
                showInfo("File is empty");
                return;
            }
            for (Book book : deserializedToBooks) {
                if (!duplicateBook(book)) {
                    data.bookList.add(book); // ← was: bookList.add(book)
                } else {
                    statusLabel.setText("Book already exists");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Error loading: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}