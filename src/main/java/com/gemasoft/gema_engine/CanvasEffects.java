package com.gemasoft.gema_engine;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class CanvasEffects extends Application {
    @Override
    public void start(Stage stage) {
        final Image image = new Image("/sprites.png");

        final Canvas canvas = new Canvas(image.getWidth() , image.getHeight());
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.drawImage(image,  0, 0);

        stage.setScene(new Scene(new Group(canvas)));
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}