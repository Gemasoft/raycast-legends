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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class DoomStyleGame extends Application {
    // Posición inicial del jugador (x, y)
    private static double  playerPosX = 4; // Posición inicial X del jugador
    private static double  playerPosY = 4; // Posición inicial Y del jugador
    private static final double MOVE_SPEED = 0.1; // Reducir velocidad a la mitad

    private static final int PLAYER_SIZE = 4; // Tamaño del punto que representa al jugador

    private static final int TILE_SIZE = 10;
    private static final int[][] MAP = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,2,2,2,2,2,0,0,0,0,3,0,3,0,3,0,0,0,1},
            {1,0,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,2,0,0,0,2,0,0,0,0,3,0,0,0,3,0,0,0,1},
            {1,0,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,2,2,0,2,2,0,0,0,0,3,0,3,0,3,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,4,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,4,0,0,0,0,5,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,4,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,4,0,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };
    private Circle player;
    private final Set<String> input = new HashSet<>();
    private double playerDirection = 0; // Ángulo de dirección del jugador en grados
    private static final int NUM_RAYS = 800;
    private static final double FOV = 70; // Campo de visión de 90 grados
    private Pane screenLayer;
    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 600;
    private static final double MAX_RAY_DISTANCE = 20.0; // Máxima distancia efectiva para el cálculo de la altura
    private final boolean showFPS = false; // Variable para controlar la visibilidad del contador de FPS
    private final Text fpsText = new Text(); // Texto para mostrar los FPS
    //private static final double JUMP_SPEED = 5; // Velocidad inicial del salto
    //private static final double GRAVITY = 0.3; // Gravedad que afecta al jugador después de saltar
    //private boolean isJumping = false;
    //private double verticalSpeed = 0; // Velocidad vertical actual del jugador

    // Cargar la textura
    //Image floorTexture = new Image("C:/Users/m1gmartin/IdeaProjects/GemaEngine/src/main/resources/wall_2.jpg");
    //Image textures = new Image("C:/Users/m1gmartin/IdeaProjects/GemaEngine/src/main/resources/wolftextures.png");


    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        drawMap(root);

        //Texture text = new Texture("C:/Users/m1gmartin/IdeaProjects/GemaEngine/src/main/resources/eagle.png", 64);

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

        // Configuración del texto para los FPS
        fpsText.setFont(new Font("Arial", 20));
        fpsText.setFill(Color.WHITE);
        fpsText.setX(10); // Posición en X (abajo a la izquierda)
        fpsText.setY(SCREEN_HEIGHT - 10); // Posición en Y
        root.getChildren().add(fpsText);

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                updatePlayerPosition();
                drawWalls();
                castRays();


                // Actualizar y mostrar los FPS
                if (now - lastUpdate >= 1_000_000_000) { // Actualizar cada segundo
                    double fps = 1_000_000_000.0 / (now - lastUpdate);
                    fpsText.setText(String.format("FPS: %f.2", fps));
                    lastUpdate = now;
                }

                // Controlar la visibilidad del texto de los FPS
                fpsText.setVisible(showFPS);
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
                 int wallType = MAP[y][x];

                switch (wallType) {
                    case 1 -> rect.setFill(Color.BLACK);
                    case 2 -> rect.setFill(Color.RED);
                    case 3 -> rect.setFill(Color.GREEN);
                    case 4 -> rect.setFill(Color.BLUEVIOLET);
                    default -> rect.setFill(Color.WHITE);
                }
                root.getChildren().add(rect);
            }
        }
    }

    // Método para alternar la visibilidad del contador de FPS
    //public void toggleFPSDisplay() {
        //showFPS = !showFPS;
    //}

    //public void playMusic(){
        /*
         // Cargar y reproducir la música de fondo
         Media md = new Media(new File("path/to/your/music.mp3").toURI().toString());
         MediaPlayer mediaPlayer = new MediaPlayer(md);
         mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Establecer para reproducir en bucle
         mediaPlayer.play();
        */
    //}

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
                if (MAP[j][i] > 0) {
                    return false; // Hay colisión con un muro
                }
            }
        }

        return true; // No hay colisión
    }

    // Método para dibujar los rayos

    private void drawWalls() {
        screenLayer.getChildren().clear(); // Limpia la capa de pantalla para las líneas verticales

        // Dibujar el cielo como un rectángulo
        //Rectangle sky = new Rectangle(0, 0, SCREEN_WIDTH, (double) SCREEN_HEIGHT);
        //sky.setFill(Color.NAVY); // Color azul marino para el cielo
        //screenLayer.getChildren().add(sky);

        double angleStep = FOV / NUM_RAYS;
        double startAngle = playerDirection - FOV / 2;

        for (int i = 0; i < NUM_RAYS; i++) {
            double rayAngle = startAngle + i * angleStep;
            double rayLength = 0;
            int hitWall = 0;
            boolean hitTexturedWall = false;

            while (hitWall == 0) {
                double checkX = playerPosX + rayLength * Math.cos(Math.toRadians(rayAngle));
                double checkY = playerPosY + rayLength * Math.sin(Math.toRadians(rayAngle));

                int mapX = (int) checkX;
                int mapY = (int) checkY;

                if (mapX < 0 || mapX >= MAP[0].length || mapY < 0 || mapY >= MAP.length) {
                    hitWall = MAP[mapY][mapX];
                } else if (MAP[mapY][mapX] > 0) {
                    hitWall = MAP[mapY][mapX];
                }
                //Cambiar grosor de lineas verticales (muros)
                if (hitWall == 0) {
                    rayLength += 0.1;
                }
            }

            double correctedRayLength = rayLength * Math.cos(Math.toRadians(rayAngle - playerDirection));
            double lineHeight = SCREEN_HEIGHT / (Math.min(correctedRayLength, MAX_RAY_DISTANCE));
            double lineTop = (SCREEN_HEIGHT - lineHeight) / 2;

            // Ajustar el color basándose en la distancia
            double brightness = 1.0 - Math.min(1.0, correctedRayLength / MAX_RAY_DISTANCE);
            Color wallColor = Color.WHITE;
            switch (hitWall){
                case 1 -> wallColor = Color.BLUE;
                case 2 -> wallColor = Color.RED;
                case 3 -> wallColor = Color.GREEN;
                case 4 -> wallColor = Color.PURPLE;
            }

            wallColor = wallColor.deriveColor(0, 1, brightness, 1);

            if (hitTexturedWall) {
                // Aquí deberías calcular qué parte de la textura mapear y luego dibujarla.
                // Este es un lugar donde necesitarás una lógica más compleja para mapear correctamente la textura.
                System.getLogger("hit Texture");
            } //else {
                Line screenLine = new Line(i, lineTop, i, lineTop + lineHeight);
            screenLine.setStroke(wallColor); // Color para muros no texturizados
                screenLayer.getChildren().add(screenLine);
            //}

            // Dibuja el piso
            Line floorLine = new Line(i, lineTop + lineHeight, i, SCREEN_HEIGHT);
            floorLine.setStroke(Color.DARKGRAY);
            screenLayer.getChildren().add(floorLine);

        }
    }

    // Método para dibujar los rayos en el mini mapa
    private void castRays() {
        // Eliminar los rayos anteriores
        ((Pane) player.getParent()).getChildren().removeIf(node -> node instanceof Line);

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
            ray.setStroke(Color.GREENYELLOW);

            ((Pane) player.getParent()).getChildren().add(ray);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
