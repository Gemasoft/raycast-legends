package com.gemasoft.gema_engine;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private static final double MOVE_SPEED = 0.1; // Reducir velocidad a la mitad

    private static final int PLAYER_SIZE = 4; // Tamaño del punto que representa al jugador

    private static final int TILE_SIZE = 10;
    private static final int[][] MAP = {
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
            {1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1 },
            {1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1 },
            {1, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1 },
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 1 },
            {1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 },
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }
    };
    private Circle player;
    private final Set<String> input = new HashSet<>();
    private Line ray;
    private static final double ROTATION_SPEED = 2.0; // Velocidad de rotación
    private double playerDirection = 0; // Ángulo de dirección del jugador en grados
    private static final int NUM_RAYS = 640;
    private static final double FOV = 70; // Campo de visión de 90 grados
    private Pane screenLayer;
    private static final int SCREEN_WIDTH = 640;
    private static final int SCREEN_HEIGHT = 480;
    private static final double MAX_RAY_DISTANCE = 20.0; // Máxima distancia efectiva para el cálculo de la altura


    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        drawMap(root);

        player = new Circle(playerPosX * TILE_SIZE + TILE_SIZE / 2.0,
                playerPosY * TILE_SIZE + TILE_SIZE / 2.0,
                PLAYER_SIZE, Color.RED);
        root.getChildren().add(player);

        screenLayer = new Pane(); // Capa para las líneas verticales
        screenLayer.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        root.getChildren().add(screenLayer);

        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        scene.setOnKeyPressed(e -> input.add(e.getCode().toString()));
        scene.setOnKeyReleased(e -> input.remove(e.getCode().toString()));

        scene.setOnMouseMoved(e -> {
            double mouseX = e.getSceneX();
            playerDirection = 360.0 / SCREEN_WIDTH * mouseX; // Ajusta la dirección basada en la posición del mouse
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updatePlayerPosition();
                drawRays();
            }
        };
        timer.start();

        // Manejador de eventos para la combinación de teclas Ctrl-F
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen()); // Cambia el estado de pantalla completa
            }
        });

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
        double dx = 0, dy = 0;

        // Calcular la dirección de avance y retroceso
        if (input.contains("W")) {
            dx += Math.cos(Math.toRadians(playerDirection)) * MOVE_SPEED;
            dy += Math.sin(Math.toRadians(playerDirection)) * MOVE_SPEED;
        }
        if (input.contains("S")) {
            dx -= Math.cos(Math.toRadians(playerDirection)) * MOVE_SPEED;
            dy -= Math.sin(Math.toRadians(playerDirection)) * MOVE_SPEED;
        }

        // Calcular la dirección de movimiento lateral (strafe)
        if (input.contains("A")) {
            dx += Math.cos(Math.toRadians(playerDirection - 90)) * MOVE_SPEED;
            dy += Math.sin(Math.toRadians(playerDirection - 90)) * MOVE_SPEED;
        }
        if (input.contains("D")) {
            dx += Math.cos(Math.toRadians(playerDirection + 90)) * MOVE_SPEED;
            dy += Math.sin(Math.toRadians(playerDirection + 90)) * MOVE_SPEED;
        }

        movePlayer(dx, dy);
        drawRays();
    }

    private void movePlayer(double dx, double dy) {
        // Intentar mover en X
        if (canMoveTo(playerPosX + dx, playerPosY)) {
            playerPosX += dx;
        }

        // Intentar mover en Y
        if (canMoveTo(playerPosX, playerPosY + dy)) {
            playerPosY += dy;
        }

        // Actualizar la posición del jugador
        player.setCenterX(playerPosX * TILE_SIZE + TILE_SIZE / 10.0);
        player.setCenterY(playerPosY * TILE_SIZE + TILE_SIZE / 20.0);
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
        screenLayer.getChildren().clear(); // Limpia la capa de pantalla para las líneas verticales

        double angleStep = FOV / NUM_RAYS;
        double startAngle = playerDirection - FOV / 2;

        for (int i = 0; i < NUM_RAYS; i++) {
            double rayAngle = startAngle + i * angleStep;
            double rayLength = 0;
            boolean hitWall = false;
            boolean hitGreenWall = false; // Indicador para un muro verde

            while (!hitWall) {
                double checkX = playerPosX + rayLength * Math.cos(Math.toRadians(rayAngle));
                double checkY = playerPosY + rayLength * Math.sin(Math.toRadians(rayAngle));

                int mapX = (int) checkX;
                int mapY = (int) checkY;

                if (mapX < 0 || mapX >= MAP[0].length || mapY < 0 || mapY >= MAP.length) {
                    hitWall = true; // Golpea el límite del mapa
                } else if (MAP[mapY][mapX] == 1) {
                    hitWall = true; // Golpea un muro normal
                } else if (MAP[mapY][mapX] == 2) {
                    hitWall = true;
                    hitGreenWall = true; // Golpea un muro verde
                }

                if (!hitWall) {
                    rayLength += 0.1;
                }
            }

            // Ajustar la longitud del rayo para corregir el efecto ojo de pez
            double correctedRayLength = rayLength * Math.cos(Math.toRadians(rayAngle - playerDirection));

            // Calcula y dibuja la línea vertical en la pantalla para representar las paredes
            double lineHeight = SCREEN_HEIGHT / (correctedRayLength > MAX_RAY_DISTANCE ? MAX_RAY_DISTANCE : correctedRayLength);
            double lineTop = (SCREEN_HEIGHT - lineHeight) / 2;
            Line screenLine = new Line(i, lineTop, i, lineTop + lineHeight);
            screenLine.setStroke(hitGreenWall ? Color.GREEN : Color.BLUE); // Color de las paredes
            screenLayer.getChildren().add(screenLine);

            // Dibuja el piso
            Line floorLine = new Line(i, lineTop + lineHeight, i, SCREEN_HEIGHT);
            floorLine.setStroke(Color.DARKGRAY); // Color del piso
            screenLayer.getChildren().add(floorLine);
        }
    }

    // Modificar el AnimationTimer para dibujar los rayos
    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            updatePlayerPosition();
            //drawRays(); // Dibujar todos los rayos
        }
    };

    public static void main(String[] args) {
        launch(args);
    }
}
