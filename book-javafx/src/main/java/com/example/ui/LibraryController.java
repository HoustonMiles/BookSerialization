package com.example.ui;

import com.example.Book;
import com.example.BookUtils;
import com.example.BinarySerializer;
import com.example.ui.BookValidator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class LibraryController {
    @FXML private TableView<Book> bookTable;

    @FXML private TableColumn<Book, String>  titleColumn;
    @FXML private TableColumn<Book, String>  authorColumn;
    @FXML private TableColumn<Book, Integer> yearColumn;
    @FXML private TableColumn<Book, String>  isbnColumn;

    @FXML private TabPane libraryTabPane;

    @FXML private TextField         searchField;
    @FXML private ChoiceBox<String> searchChoiceBox;

    @FXML private MenuButton sortListButton;
    @FXML private Label      statusLabel;
    @FXML private Button     removeButton;

    // Changed from ToggleButton to CheckBox so it visually shows on/off state
    @FXML private CheckBox verifiedOnlyCheckBox;

    // Injected automatically by FXMLLoader: fx:id="bookForm" → bookFormController
    @FXML private BookController bookFormController;

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
        // Tooltip on Remove button
        Tooltip tooltip = new Tooltip("Select a book in the table, then click the Remove Book button.");
        tooltip.setShowDelay(javafx.util.Duration.millis(100));
        tooltip.setShowDuration(javafx.util.Duration.INDEFINITE);
        removeButton.setTooltip(tooltip);

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

        MenuItem authorItem = new MenuItem("Author Ordered");
        MenuItem titleItem  = new MenuItem("Title Ordered");
        MenuItem yearItem   = new MenuItem("Year Ordered");
        MenuItem isbnItem   = new MenuItem("ISBN Ordered");

        searchChoiceBox.getItems().addAll("Search Author", "Search Title", "Search Year", "Search ISBN");
        searchChoiceBox.setValue("Search Author");
        searchField.setPromptText("Search Here!");

        // Re-apply filters whenever the search text, search mode, or checkbox changes
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        searchChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { searchField.clear(); applyFilters(); }
        });

        // CheckBox listener — re-apply filters when ticked or unticked
        verifiedOnlyCheckBox.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                applyFilters());

        // Re-apply filters when switching tabs so the checkbox state is respected on the new tab
        libraryTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) ->
                applyFilters());

        authorItem.setOnAction(e -> { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_AUTHOR); });
        titleItem .setOnAction(e -> { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_TITLE); });
        yearItem  .setOnAction(e -> { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_YEAR); });
        isbnItem  .setOnAction(e -> { LibraryTabData d = currentData(); if (d != null) d.sortedList.setComparator(Book.BY_ISBN); });

        sortListButton.getItems().addAll(authorItem, titleItem, yearItem, isbnItem);
        statusLabel.setText("Ready");

        if (bookFormController != null) {
            bookFormController.setLibraryController(this);
        }
    }

    private void setupContextMenu(TableView<Book> table, LibraryTabData sourceData) {
        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();

            Menu moveToMenu = new Menu("Move to Library");

            // Rebuild the submenu every time it opens so it reflects current tabs
            contextMenu.setOnShowing(e -> {
                moveToMenu.getItems().clear();
                for (Map.Entry<Tab, LibraryTabData> entry : tabDataMap.entrySet()) {
                    // Don't show the tab the book is already in
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
                // If there are no other tabs, show a disabled placeholder
                if (moveToMenu.getItems().isEmpty()) {
                    MenuItem none = new MenuItem("No other libraries");
                    none.setDisable(true);
                    moveToMenu.getItems().add(none);
                }
            });

            contextMenu.getItems().add(moveToMenu);

            // Only show the context menu on non-empty rows
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
            // Verified-only gate — hide unverified books when checkbox is ticked
            if (verifiedOnly && !book.isVerified()) return false;

            // Search gate — empty/blank search shows everything that passed the first gate
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
            setupContextMenu(newTable, tabDataMap.get(tab));
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

    // Overload that checks against a specific tab's list — used inside background
    // tasks where currentData() might point to a different tab than the one being loaded
    private boolean duplicateInList(Book book, ObservableList<Book> list) {
        for (Book b : list) {
            if (book.equals(b)) return true;
        }
        return false;
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
            statusLabel.setText("Book removed!");
        }
    }

    @FXML
    private void handleSaveButtonAction() {
        try {
            LibraryTabData data = currentData();
            if (data == null) return;
            if (data.bookList.isEmpty()) { showInfo("Table is empty"); return; }

            FileChooser fc = new FileChooser();
            fc.setTitle("Save File");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialization Files", "*.xml", "*.csv", "*.bin"));
            fc.setInitialFileName(libraryTabPane.getSelectionModel().getSelectedItem().getText());
            File file = fc.showSaveDialog(statusLabel.getScene().getWindow());
            if (file == null) return;

            TreeSet<Book> bookSet = new TreeSet<>(data.bookList);
            if      (file.getName().endsWith(".csv")) BookUtils.serializeToCSV(bookSet, file);
            else if (file.getName().endsWith(".xml")) BookUtils.serializeToXML(bookSet, file);
            else if (file.getName().endsWith(".bin")) BinarySerializer.binarySerialize(bookSet, file);

        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLoadButtonAction() {
        LibraryTabData data = currentData();
        if (data == null) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Open File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Serialization Files", "*.xml", "*.csv", "*.bin"));
        File file = fc.showOpenDialog(statusLabel.getScene().getWindow());
        if (file == null) return;

        Set<Book> loaded = null;
        try {
            if      (file.getName().endsWith(".csv")) loaded = BookUtils.deserializeFromCSV(file);
            else if (file.getName().endsWith(".xml")) loaded = BookUtils.deserializeFromXML(file);
            else if (file.getName().endsWith(".bin")) {
                loaded = (Set<Book>) BinarySerializer.binaryDeserialize(file);
                if (loaded == null) loaded = new TreeSet<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            statusLabel.setText("Error loading: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (loaded == null || loaded.isEmpty()) { showInfo("File is empty"); return; }

        // Filter duplicates before starting the network task
        List<Book> toValidate = new ArrayList<>();
        for (Book b : loaded) {
            if (!duplicateInList(b, data.bookList)) toValidate.add(b);
            else statusLabel.setText("Skipped duplicate(s)");
        }

        if (toValidate.isEmpty()) { showInfo("All books in the file already exist."); return; }

        statusLabel.setText("Loaded " + toValidate.size() + " book(s). Validating with Open Library…");

        Task<List<Book>> validateTask = new Task<>() {
            @Override
            protected List<Book> call() {
                List<Book> completed = new ArrayList<>();
                for (int i = 0; i < toValidate.size(); i++) {
                    Book b = toValidate.get(i);
                    BookValidator.ValidationResult r = BookValidator.validate(b);
                    // r.completedBook has verified=true when found; original book (verified=false) when not
                    completed.add(r.found ? r.completedBook : b);
                    updateProgress(i + 1, toValidate.size());
                    updateMessage("Validating " + (i + 1) + " / " + toValidate.size() + "…");
                }
                return completed;
            }
        };

        validateTask.messageProperty().addListener((obs, o, n) -> statusLabel.setText(n));

        validateTask.setOnSucceeded(e -> {
            List<Book> completedBooks = validateTask.getValue();
            int added = 0, verified = 0;
            for (Book b : completedBooks) {
                // FIX: use duplicateInList against the captured data.bookList, not
                // currentData(), so we always check the correct tab even if the user
                // has switched tabs while the background task was running
                if (!duplicateInList(b, data.bookList)) {
                    data.bookList.add(b);
                    added++;
                    if (b.isVerified()) verified++;
                }
            }
            statusLabel.setText("Added " + added + " book(s), " + verified + " verified by Open Library.");
        });

        validateTask.setOnFailed(e ->
                statusLabel.setText("Validation error: " + validateTask.getException().getMessage()));

        new Thread(validateTask, "load-validator").start();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}