package com.example.hellofx;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloJavaFX extends Application {
    @Override
    public void start(Stage stage) {
        // Requirement 2: Change message to welcome your name
        Label message = new Label("Welcome, Musenga Hope Sichula!");

        // Requirement 3: Change button text from "Click Me" to "Start"
        Button startButton = new Button("Start");
        startButton.setOnAction(event ->
                message.setText("Great! You clicked the button.")
        );

        // Requirement 4: Add a second button called "Reset"
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(event ->
                message.setText("Welcome, Musenga!")
        );

        // Put both buttons side by side in an HBox
        HBox buttonBox = new HBox(10, startButton, resetButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(message, buttonBox);

        Scene scene = new Scene(layout, 500, 300);

        // Requirement 1: Change window title to include your student number
        stage.setTitle("My First JavaFX Application - Student ID:202511308 ");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}