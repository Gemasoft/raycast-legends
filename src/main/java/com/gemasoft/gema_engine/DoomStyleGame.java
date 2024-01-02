package com.gemasoft.gema_engine;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class DoomStyleGame extends Application {
    // Posición inicial del jugador (x, y)
    private static double  playerPosX = 7; // Posición inicial X del jugador
    private static double  playerPosY = 5; // Posición inicial Y del jugador
    private static final double MOVE_SPEED = 0.2; // Reducir velocidad a la mitad

    private static final int PLAYER_SIZE = 4; // Tamaño del punto que representa al jugador

    private static final int TILE_SIZE = 10;
    private static final int[][] MAP = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 1},
            {1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1},
            {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}
    };
    private Circle player;
    private final Set<String> input = new HashSet<>();
    private Line ray;
    private static final double ROTATION_SPEED = 2.0; // Velocidad de rotación
    private double playerDirection = 0; // Ángulo de dirección del jugador en grados
    private static final int NUM_RAYS = 256;
    private static final double FOV = 90; // Campo de visión de 90 grados



    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        drawMap(root);

        player = new Circle(playerPosX * TILE_SIZE + TILE_SIZE / 2.0,
                playerPosY * TILE_SIZE + TILE_SIZE / 2.0,
                PLAYER_SIZE, Color.RED);
        root.getChildren().add(player);

        Scene scene = new Scene(root, 640, 480);
        scene.setOnKeyPressed(e -> input.add(e.getCode().toString()));
        scene.setOnKeyReleased(e -> input.remove(e.getCode().toString()));

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updatePlayerPosition();
                drawRays(); // Dibujar el rayo en cada actualización
            }
        };
        timer.start();

        primaryStage.setTitle("Doom Style Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawMap(Pane root) {
        for (int y = 0; y < MAP.length; y++) {
            for (int x = 0; x < MAP[y].length; x++) {
                Rectangle rect = new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                rect.setFill(MAP[y][x] == 1 ? Color.BLACK : Color.WHITE);
                root.getChildren().add(rect);
            }
        }
    }

    private void updatePlayerPosition() {
        if (input.contains("A")) {
            playerDirection -= ROTATION_SPEED;
        }
        if (input.contains("D")) {
            playerDirection += ROTATION_SPEED;
        }

        double dx = 0, dy = 0;
        if (input.contains("W")) {
            dx = Math.cos(Math.toRadians(playerDirection)) * MOVE_SPEED;
            dy = Math.sin(Math.toRadians(playerDirection)) * MOVE_SPEED;
        }
        if (input.contains("S")) {
            dx = -Math.cos(Math.toRadians(playerDirection)) * MOVE_SPEED;
            dy = -Math.sin(Math.toRadians(playerDirection)) * MOVE_SPEED;
        }

        movePlayer(dx, dy);
        drawRays(); // Asegúrate de redibujar el rayo con la nueva orientación
    }

    private void movePlayer(double dx, double dy) {
        double newX = playerPosX + dx;
        double newY = playerPosY + dy;

        if (canMoveTo(newX, newY)) {
            playerPosX = newX;
            playerPosY = newY;
            player.setCenterX(playerPosX * TILE_SIZE + TILE_SIZE / 10.0);
            player.setCenterY(playerPosY * TILE_SIZE + TILE_SIZE / 20.0);
        }
    }

    private boolean canMoveTo(double x, double y) {
        int leftTile = (int) Math.floor(x - PLAYER_SIZE / 2.0 / TILE_SIZE);
        int rightTile = (int) Math.ceil(x + PLAYER_SIZE / 0.8 / TILE_SIZE) - 1 ; // Ajuste para incluir el borde derecho
        int topTile = (int) Math.floor(y - PLAYER_SIZE / 2.0 / TILE_SIZE);
        int bottomTile = (int) Math.ceil(y + PLAYER_SIZE / 0.8 / TILE_SIZE) - 1; // Ajuste para incluir el borde inferior

        if (leftTile < 0 || rightTile >= MAP[0].length || topTile < 0 || bottomTile >= MAP.length) {
            return false; // Fuera del mapa
        }

        for (int i = leftTile; i <= rightTile; i++) {
            for (int j = topTile; j <= bottomTile; j++) {
                if (MAP[j][i] == 1) {
                    return false; // Hay colisión con un muro
                }
            }
        }

        return true; // No hay colisión
    }

    // Método para dibujar los rayos
    private void drawRays() {
        // Eliminar los rayos anteriores
        ((Pane) player.getParent()).getChildren().removeIf(node -> node instanceof Line && node != player);

        double angleStep = FOV / NUM_RAYS;
        double startAngle = playerDirection - FOV / 2;

        for (int i = 0; i < NUM_RAYS; i++) {
            double rayAngle = startAngle + i * angleStep;
            double rayLength = 0;

            while (true) {
                double checkX = playerPosX + rayLength * Math.cos(Math.toRadians(rayAngle));
                double checkY = playerPosY + rayLength * Math.sin(Math.toRadians(rayAngle));

                int mapX = (int) checkX;
                int mapY = (int) checkY;

                if (mapX < 0 || mapX >= MAP[0].length || mapY < 0 || mapY >= MAP.length || MAP[mapY][mapX] == 1) {
                    break;
                }

                rayLength += 0.1;
            }

            Line ray = new Line(player.getCenterX(), player.getCenterY(),
                    player.getCenterX() + rayLength * TILE_SIZE * Math.cos(Math.toRadians(rayAngle)),
                    player.getCenterY() + rayLength * TILE_SIZE * Math.sin(Math.toRadians(rayAngle)));
            ray.setStroke(Color.LIGHTGRAY);

            ((Pane) player.getParent()).getChildren().add(ray);
        }
    }

    // Modificar el AnimationTimer para dibujar los rayos
    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            updatePlayerPosition();
            drawRays(); // Dibujar todos los rayos
        }
    };


    // Métodos adicionales dentro de la clase DoomStyleGame

    private void initializePlayer() {
        // Inicializa la posición y dirección del jugador
    }

    private void addMovementControls() {
        // Añade controles para el movimiento del jugador
    }

    private void performRayCasting() {
        // Realiza cálculos de raycasting para la vista en primera persona
    }

    private void draw3DView() {
        // Dibuja la vista 3D basada en los cálculos de raycasting
    }

    public static void main(String[] args) {
        launch(args);
    }
}
