package com.example.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader homeLoader = new FXMLLoader(getClass().getResource("Main.fxml"));
        Scene scene = new Scene(homeLoader.load());
        //scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        primaryStage.setTitle("Book Library Manager");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        // Side panel width (must match your CSS)
        double sidePanelWidth = 250;

        // Calculate center position for the main content (excluding side panel)
        double windowWidth = primaryStage.getWidth();
        double windowHeight = primaryStage.getHeight();

        // Center the main content area (shift left by half the side panel width)
        double centerX = (screenBounds.getWidth() - windowWidth) / 2 - (sidePanelWidth / 2);
        double centerY = (screenBounds.getHeight() - windowHeight) / 2;

        // Set the window position
        primaryStage.setX(centerX);
        primaryStage.setY(centerY);
    }

    public static void main(String[] args) {
        launch(args);
    }
}