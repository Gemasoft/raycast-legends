package com.gemasoft.gema_engine;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Wireframe extends Application {

    @Override
    public void start(Stage primaryStage) {
        Group root = new Group();
        Scene scene = new Scene(root, 600, 400, true);
        scene.setFill(Color.BLACK);

        // Coordenadas de los vértices del triángulo base
        double[] xPoints = {0, 100, 50};
        double[] yPoints = {0, 0, 86.6};

        // Punto central (cúspide de la pirámide)
        double peakX = 50;
        double peakY = 43.3;
        double peakZ = 75;

        // Crear líneas para formar la base del triángulo
        Line baseLine1 = new Line(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
        Line baseLine2 = new Line(xPoints[1], yPoints[1], xPoints[2], yPoints[2]);
        Line baseLine3 = new Line(xPoints[2], yPoints[2], xPoints[0], yPoints[0]);

        // Crear líneas que conectan la base con la cúspide
        Line sideLine1 = new Line(xPoints[0], yPoints[0], peakX, peakY);
        Line sideLine2 = new Line(xPoints[1], yPoints[1], peakX, peakY);
        Line sideLine3 = new Line(xPoints[2], yPoints[2], peakX, peakY);

        // Establecer el color y grosor de las líneas
        setLineProperties(baseLine1, baseLine2, baseLine3, sideLine1, sideLine2, sideLine3);

        // Añadir las líneas al grupo pyramid
        Group pyramid = new Group(baseLine1, baseLine2, baseLine3, sideLine1, sideLine2, sideLine3);
        pyramid.setLayoutX(scene.getWidth() / 2);
        pyramid.setLayoutY(scene.getHeight() / 2);

        root.getChildren().add(pyramid);

        // Configuraciones de rotación y animación
        configureRotationAndAnimation(pyramid, scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("3D Rotating Rainbow Pyramid");
        primaryStage.show();
    }

    private void setLineProperties(Line... lines) {
        for (Line line : lines) {
            line.setStrokeWidth(3);
            line.setStroke(Color.RED);
        }
    }

    private void configureRotationAndAnimation(Group pyramid, Scene scene) {
        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
        pyramid.getTransforms().addAll(rotateX, rotateY, rotateZ);

        Timeline rotationTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotateX.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(3), new KeyValue(rotateX.angleProperty(), 360)),
                new KeyFrame(Duration.ZERO, new KeyValue(rotateY.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(4), new KeyValue(rotateY.angleProperty(), 360)),
                new KeyFrame(Duration.ZERO, new KeyValue(rotateZ.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(5), new KeyValue(rotateZ.angleProperty(), 360))
        );
        rotationTimeline.setCycleCount(Timeline.INDEFINITE);
        rotationTimeline.play();

        // Animación de color
        Timeline colorTimeline = new Timeline();
        for (Node node : pyramid.getChildren()) {
            if (node instanceof Line) {
                Line line = (Line) node;
                colorTimeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO, new KeyValue(line.strokeProperty(), Color.RED, Interpolator.EASE_BOTH)),
                        new KeyFrame(Duration.seconds(3), new KeyValue(line.strokeProperty(), Color.BLUE, Interpolator.EASE_BOTH))
                );
            }
        }
        colorTimeline.setCycleCount(Timeline.INDEFINITE);
        colorTimeline.setAutoReverse(true);
        colorTimeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
