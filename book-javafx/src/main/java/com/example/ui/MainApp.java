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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Library.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage.setTitle("Book Library Manager");
        primaryStage.setScene(scene);
        primaryStage.show();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double windowWidth  = primaryStage.getWidth();
        double windowHeight = primaryStage.getHeight();
        primaryStage.setX((screenBounds.getWidth()  - windowWidth)  / 2);
        primaryStage.setY((screenBounds.getHeight() - windowHeight) / 2);
    }

    public static void main(String[] args) {
        launch(args);
    }
}