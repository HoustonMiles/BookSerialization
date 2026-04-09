package com.example.ui;

import com.example.Book;
import com.example.BookUtils;
import com.example.BinarySerializer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LibraryController {

    @FXML private TableView<Book> bookTable;

    @FXML private TableColumn<Book, String>  titleColumn;
    @FXML private TableColumn<Book, String>  authorColumn;
    @FXML private TableColumn<Book, Integer> yearColumn;
    @FXML private TableColumn<Book, String>  isbnColumn;

    @FXML private TabPane libraryTabPane;

    @FXML private TextField         searchField;
    @FXML private ChoiceBox<String> searchChoiceBox;
    @FXML private CheckBox          verifiedOnlyCheckBox;
    @FXML private Label             statusLabel;

    private static class LibraryTabData {
        final ObservableList<Book> bookList;
        final FilteredList<Book>   filteredList;
        final SortedList<Book>     sortedList;
        final TableView<Book>      tableView;

        LibraryTabData(TableView<Book> tableView) {
            this.tableView = tableView;
            this.tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            this.bookList     = FXCollections.observableArrayList();
            this.filteredList = new FilteredList<>(bookList, p -> true);
            this.sortedList   = new SortedList<>(filteredList);
            tableView.setItems(sortedList);
        }
    }

    private final Map<Tab, LibraryTabData> tabDataMap = new HashMap<>();
    private int tabCounter = 2;

    private LibraryTabData currentData() {
        Tab selected = libraryTabPane.getSelectionModel().getSelectedItem();
        return selected == null ? null : tabDataMap.get(selected);
    }

    @FXML
    private void initialize() {
        bookTable.setEditable(true);

        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        titleColumn.setOnEditCommit(t -> t.getTableView().getItems()
                .get(t.getTablePosition().getRow()).setTitle(t.getNewValue()));

        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        authorColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        authorColumn.setOnEditCommit(t -> t.getTableView().getItems()
                .get(t.getTablePosition().getRow()).setAuthor(t.getNewValue()));

        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
        yearColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        yearColumn.setOnEditCommit(t -> t.getTableView().getItems()
                .get(t.getTablePosition().getRow()).setYear(t.getNewValue()));

        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        isbnColumn.setOnEditCommit(t -> t.getTableView().getItems()
                .get(t.getTablePosition().getRow()).setIsbn(t.getNewValue()));

        Tab firstTab = libraryTabPane.getTabs().get(0);
        firstTab.setText("Library 1");
        tabDataMap.put(firstTab, new LibraryTabData(bookTable));
        setupContextMenu(bookTable, tabDataMap.get(firstTab));

        searchChoiceBox.getItems().addAll("Search Author", "Search Title", "Search Year", "Search ISBN");
        searchChoiceBox.setValue("Search Author");
        searchField.setPromptText("Search…");

        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        searchChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null) { searchField.clear(); applyFilters(); }
        });
        verifiedOnlyCheckBox.selectedProperty().addListener((obs, o, n) -> applyFilters());
        libraryTabPane.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> applyFilters());

        statusLabel.setText("Ready");
    }

    private void setupContextMenu(TableView<Book> table, LibraryTabData sourceData) {
        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            Menu moveToMenu = new Menu("Move to Library");

            contextMenu.setOnShowing(e -> {
                moveToMenu.getItems().clear();
                for (Map.Entry<Tab, LibraryTabData> entry : tabDataMap.entrySet()) {
                    if (entry.getValue() == sourceData) continue;
                    MenuItem item = new MenuItem(entry.getKey().getText());
                    LibraryTabData targetData = entry.getValue();
                    item.setOnAction(ev -> {
                        Book selected = row.getItem();
                        if (selected == null) return;
                        if (duplicateInList(selected, targetData.bookList)) {
                            statusLabel.setText("Book already exists in that library.");
                            return;
                        }
                        targetData.bookList.add(selected);
                        sourceData.bookList.remove(selected);
                        statusLabel.setText("Book moved to " + entry.getKey().getText());
                    });
                    moveToMenu.getItems().add(item);
                }
                if (moveToMenu.getItems().isEmpty()) {
                    MenuItem none = new MenuItem("No other libraries");
                    none.setDisable(true);
                    moveToMenu.getItems().add(none);
                }
            });

            contextMenu.getItems().add(moveToMenu);
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });
    }

    private void applyFilters() {
        LibraryTabData data = currentData();
        if (data == null) return;

        boolean verifiedOnly = verifiedOnlyCheckBox.isSelected();
        String  searchText   = searchField.getText();
        String  searchMode   = searchChoiceBox.getValue();

        data.filteredList.setPredicate(book -> {
            if (verifiedOnly && !book.isVerified()) return false;
            if (searchText == null || searchText.isBlank()) return true;
            String q = searchText.toLowerCase().trim();
            return switch (searchMode) {
                case "Search Author" -> book.getAuthor().toLowerCase().contains(q);
                case "Search Title"  -> book.getTitle().toLowerCase().contains(q);
                case "Search Year"   -> String.valueOf(book.getYear()).contains(q);
                case "Search ISBN"   -> book.getIsbn().toLowerCase().contains(q);
                default -> true;
            };
        });
    }

    public String addBook(Book book) {
        LibraryTabData data = currentData();
        if (data == null) return "No active library.";
        if (duplicateBook(book)) return "Book already exists";
        data.bookList.add(book);
        statusLabel.setText("Book added");
        return "Book added";
    }

    // Menu handlers

    @FXML
    private void handleAddBookAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Book.fxml"));
            VBox content = loader.load();
            BookController bookController = loader.getController();
            bookController.setLibraryController(this);

            Stage dialog = new Stage();
            dialog.setTitle("Add Book");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(content));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error opening Add Book dialog.");
        }
    }

    @FXML
    private void handleEditBookAction() {
        LibraryTabData data = currentData();
        if (data == null) return;
        Book selectedBook = data.tableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showInfo("Please select a book.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Book.fxml"));
            VBox content = loader.load();
            BookController bookController = loader.getController();
            bookController.setLibraryController(this);
            bookController.setFields(selectedBook);

            Stage dialog = new Stage();
            dialog.setTitle("Edit Book");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(content));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error opening Edit Book dialog.");
        }
    }

    @FXML
    private void handleNewTabAction() {
        TextInputDialog dialog = new TextInputDialog("Library " + tabCounter);
        dialog.setTitle("New Library");
        dialog.setHeaderText(null);
        dialog.setContentText("Library name:");
        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) return;

            TableView<Book> newTable = new TableView<>();
            newTable.setEditable(true);
            newTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            TableColumn<Book, String>  tc = new TableColumn<>("Title");
            TableColumn<Book, String>  ac = new TableColumn<>("Author");
            TableColumn<Book, Integer> yc = new TableColumn<>("Year");
            TableColumn<Book, String>  ic = new TableColumn<>("ISBN");

            tc.setPrefWidth(220);
            ac.setPrefWidth(180);
            yc.setPrefWidth(80);
            ic.setPrefWidth(150);

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

            Tab tab = new Tab(name.trim(), tabContent);
            tab.setClosable(false);
            tabDataMap.put(tab, new LibraryTabData(newTable));
            setupContextMenu(newTable, tabDataMap.get(tab));
            libraryTabPane.getTabs().add(tab);
            libraryTabPane.getSelectionModel().select(tab);
            tabCounter++;
            statusLabel.setText("Created library: " + name.trim());
        });
    }

    @FXML
    private void handleEditTabAction() {
        Tab selected = libraryTabPane.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        TextInputDialog dialog = new TextInputDialog(selected.getText());
        dialog.setTitle("Edit Library Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Library name:");
        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) return;
            selected.setText(name.trim());
            statusLabel.setText("Edited library: " + name.trim());
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

    @FXML
    private void handleRemoveButtonAction() {
        LibraryTabData data = currentData();
        if (data == null) return;
        Book selectedBook = data.tableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showInfo("Please select a book.");
        } else {
            data.bookList.remove(selectedBook);
            statusLabel.setText("Book removed.");
        }
    }

    @FXML private void handleSortByAuthor() { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_AUTHOR); }
    @FXML private void handleSortByTitle()  { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_TITLE);  }
    @FXML private void handleSortByYear()   { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_YEAR);   }
    @FXML private void handleSortByIsbn()   { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_ISBN);   }

    @FXML
    private void handleSaveButtonAction() {
        try {
            LibraryTabData data = currentData();
            if (data == null) return;
            if (data.bookList.isEmpty()) { showInfo("Table is empty"); return; }

            FileChooser fc = new FileChooser();
            fc.setTitle("Save File");
            fc.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("CSV (.csv)", "*.csv"),
                    new FileChooser.ExtensionFilter("XML (.xml)", "*.xml"),
                    new FileChooser.ExtensionFilter("Binary (.bin)", "*.bin")
            );
            fc.setInitialFileName(libraryTabPane.getSelectionModel().getSelectedItem().getText());
            File file = fc.showSaveDialog(statusLabel.getScene().getWindow());
            if (file == null) return;

            // On Linux the file chooser doesn't append the extension automatically
            String ext = switch (fc.getSelectedExtensionFilter().getDescription()) {
                case "XML (.xml)" -> ".xml";
                case "Binary (.bin)" -> ".bin";
                default -> ".csv";
            };
            if (!file.getName().contains(".")) file = new File(file.getAbsolutePath() + ext);

            TreeSet<Book> bookSet = new TreeSet<>(data.bookList);
            if (file.getName().endsWith(".csv")) BookUtils.serializeToCSV(bookSet, file);
            else if (file.getName().endsWith(".xml")) BookUtils.serializeToXML(bookSet, file);
            else if (file.getName().endsWith(".bin")) BinarySerializer.binarySerialize(bookSet, file);

            statusLabel.setText("Saved to " + file.getName());
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLoadButtonAction() {
        LibraryTabData data = currentData();
        if (data == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Open File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Library Files", "*.xml", "*.csv", "*.bin"));
        File file = fc.showOpenDialog(statusLabel.getScene().getWindow());
        if (file == null) return;

        Set<Book> loaded = null;
        try {
            if (file.getName().endsWith(".csv")) loaded = BookUtils.deserializeFromCSV(file);
            else if (file.getName().endsWith(".xml")) loaded = BookUtils.deserializeFromXML(file);
            else if (file.getName().endsWith(".bin")) {
                loaded = (Set<Book>) BinarySerializer.binaryDeserialize(file);
                if (loaded == null) loaded = new TreeSet<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Error loading: " + e.getMessage());
            return;
        }

        if (loaded == null || loaded.isEmpty()) { showInfo("File is empty"); return; }

        List<Book> toValidate = new ArrayList<>();
        for (Book b : loaded) {
            if (!duplicateInList(b, data.bookList)) toValidate.add(b);
            else statusLabel.setText("Skipped duplicate(s)");
        }

        if (toValidate.isEmpty()) { showInfo("All books already exist."); return; }

        statusLabel.setText("Validating " + toValidate.size() + " book(s) with Open Library…");

        Task<List<Book>> validateTask = new Task<>() {
            @Override
            protected List<Book> call() {
                List<Book> completed = new ArrayList<>();
                for (int i = 0; i < toValidate.size(); i++) {
                    Book b = toValidate.get(i);
                    BookValidator.ValidationResult r = BookValidator.validate(b);
                    completed.add(r.found ? r.completedBook : b);
                    updateMessage("Validating " + (i + 1) + " / " + toValidate.size() + "…");
                }
                return completed;
            }
        };

        validateTask.messageProperty().addListener((obs, o, n) -> statusLabel.setText(n));
        validateTask.setOnSucceeded(e -> {
            int added = 0, verified = 0;
            for (Book b : validateTask.getValue()) {
                if (!duplicateInList(b, data.bookList)) {
                    data.bookList.add(b);
                    added++;
                    if (b.isVerified()) verified++;
                }
            }
            statusLabel.setText("Added " + added + " book(s), " + verified + " verified.");
        });
        validateTask.setOnFailed(e ->
                statusLabel.setText("Validation error: " + validateTask.getException().getMessage()));

        new Thread(validateTask, "load-validator").start();
    }

    // Helpers

    public void refreshTable() {
        LibraryTabData data = currentData();
        if (data != null) data.tableView.refresh();
    }

    private boolean duplicateBook(Book book) {
        LibraryTabData data = currentData();
        if (data == null) return false;
        return duplicateInList(book, data.bookList);
    }

    private boolean duplicateInList(Book book, ObservableList<Book> list) {
        for (Book b : list) { if (book.equals(b)) return true; }
        return false;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}