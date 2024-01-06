package com.gemasoft.gema_engine;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.*;

public class DoomStyleGame extends Application {
    private static final int[][] MAP = {
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,0,0,0,0,0,9,2,2,2,2,0,0,0,0,3,0,3,0,3,0,0,0,1},
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
            {1,4,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,9,0,0,0,1},
            {1,4,0,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,9,0,0,0,0,1},
            {1,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,4,4,4,4,4,4,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };
    private static final double ROTATION_FACTOR = 0.8;
    private static final int MAX_VERTICAL_ANGLE = 750;
    // Posición inicial del jugador (x, y)
    private static double  playerPosX = 3; // Posición inicial X del jugador
    private static double  playerPosY = 3; // Posición inicial Y del jugador
    private static final int SCALE = 1;
    private static final double MOVE_SPEED = 0.1; // Reducir velocidad a la mitad
    private static final int PLAYER_SIZE = 1; // Tamaño del punto que representa al jugador
    private static final int TILE_SIZE = 8 * SCALE;
    private Circle player = new Circle(playerPosX * TILE_SIZE + TILE_SIZE / 2.0,
                                     playerPosY * TILE_SIZE + TILE_SIZE / 2.0,
                                     PLAYER_SIZE, Color.WHITE);
    private final Set<String> input = new HashSet<>();
    private double playerDirection = 0; // Ángulo de dirección del jugador en grados
    private static final double FOV = 75; // Campo de visión de 90 grados
    private Pane screenLayer;
    private static int SCREEN_WIDTH = 800;
    //private static final int NUM_RAYS = 1200;
    private static int SCREEN_HEIGHT = 600;
    private static final double MAX_RAY_DISTANCE = 10.0; // Máxima distancia efectiva para el cálculo de la altura
    private Polygon playerTriangle = new Polygon();;
    private List<Sprite> sprites = new ArrayList<>();
    private Canvas canvas;
    private GraphicsContext gc;
    private ImageView weaponImage;
    private boolean playerIsMoving;
    private static double VISION_ANGLE = 0;
    private final double ANGLE_CHANGE_FACTOR = 2; // Ajusta este valor según la sensibilidad deseada
    private static double MID_VERTICAL_SCREEN = (double) SCREEN_WIDTH / 2;
    private AnimationTimer slowRenderingTimer;
    private double swordYOffset = 0;
    private boolean movingUp = true;
    private final double SWORD_MOVEMENT_SPEED = 0.5; // Ajusta la velocidad de movimiento
    private double lastMouseX = -1; // Inicializar con un valor no válido
    private double lastMouseY = -1; // Para rastrear la última posición Y del ratón
    private long lastUpdate = 0;
    private final long DELAY = 10000000; // Retraso en nanosegundos

    @Override
    public void start(Stage primaryStage) {
        // Configurar pantalla completa
        primaryStage.setFullScreen(false);
        Pane root = new Pane();
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

        screenLayer = new Pane(); // Capa para las líneas verticales
        screenLayer.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
        root.getChildren().add(screenLayer);

        // Esconde el cursor del mouse
        scene.setCursor(Cursor.CROSSHAIR);
        scene.setOnKeyPressed(e -> input.add(e.getCode().toString()));
        scene.setOnKeyReleased(e -> input.remove(e.getCode().toString()));

        // Manejador de movimiento vertical con mouse
        scene.setOnMouseMoved(e -> {
            if (lastMouseX >= 0) {
                double deltaX = e.getSceneX() - lastMouseX;
                playerDirection += deltaX * ROTATION_FACTOR;
            }

            if (lastMouseY >= 0) {
                double deltaY = e.getSceneY() - lastMouseY;
                // Aumentar/reducir VISION_ANGLE basado en el movimiento del ratón
                VISION_ANGLE += deltaY * -ANGLE_CHANGE_FACTOR;

                // Opcional: Limitar VISION_ANGLE a un rango específico
                if (VISION_ANGLE > MAX_VERTICAL_ANGLE ){
                    VISION_ANGLE = MAX_VERTICAL_ANGLE;
                } else if(VISION_ANGLE < -MAX_VERTICAL_ANGLE){
                    VISION_ANGLE = -MAX_VERTICAL_ANGLE;
                }
            }

            if (!isMouseAtCenter(e)) {
                centerMouse(primaryStage, scene);
            } else {
                lastMouseX = e.getSceneX();
                lastMouseY = e.getSceneY();
            }
        });

        // Manejador de eventos para la combinación de teclas Ctrl-F
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.F) {
                primaryStage.setFullScreen(!primaryStage.isFullScreen()); // Cambia el estado de pantalla completa
            } else if (event.isControlDown() && event.getCode() == KeyCode.X) {
                primaryStage.close();
            }
        });

        // Manejo de cambios en el tamano de pantalla (Ancho)
        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Ancho de la ventana cambiado: " + newValue);
                adjustCanvas(primaryStage);
            }
        });
        // Manejo de cambios en el tamano de pantalla (Alto)
        primaryStage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                System.out.println("Alto de la ventana cambiado: " + newValue);
                adjustCanvas(primaryStage);
            }
        });
        // Posición inicial del triángulo
        updatePlayerPosition();
        // Titulo de la ventana
        primaryStage.setTitle("Raycast Legends");
        primaryStage.setScene(scene);
        primaryStage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updatePlayerPosition();
                drawWalls();
                //castRays();
            }
        };
        drawMiniMap(root);
        timer.start();
    }
    private boolean isMouseAtCenter(MouseEvent e) {
        double centerX = SCREEN_WIDTH / 2;
        double centerY = SCREEN_HEIGHT / 2;
        return e.getSceneX() == centerX && e.getSceneY() == centerY;
    }
    // Metodo para centar el cursos del mouse
    private void centerMouse(Stage primaryStage, Scene scene) {
        Robot robot = new Robot();
        double centerX = primaryStage.getX() + scene.getWidth() / 2;
        double centerY = primaryStage.getY() + scene.getHeight() / 2;

        // Considerar la barra de título de la ventana en el cálculo de Y
        double titleBarHeight = primaryStage.getHeight() - scene.getHeight();
        centerY += titleBarHeight;

        robot.mouseMove(centerX, centerY);
        lastMouseX = -1; // Restablecer lastMouseX
    }
    // Metodo para actualizar los valores del tamano de la pantalla
    private void adjustCanvas(Stage primaryStage){
        MID_VERTICAL_SCREEN = primaryStage.getHeight() / 2;
        SCREEN_HEIGHT = (int) primaryStage.getHeight();
        SCREEN_WIDTH = (int) primaryStage.getWidth();
    }
    private void drawMiniMap(Pane root) {
        for (int y = 0; y < MAP.length; y++) {
            for (int x = 0; x < MAP[y].length; x++) {
                Rectangle rect = new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                int wallType = MAP[y][x];
                switch (wallType) {
                    case 1 -> rect.setFill(Color.rgb(0,0,0,0.5));
                    case 2 -> rect.setFill(Color.rgb(255, 0, 0, 0.5));
                    case 3 -> rect.setFill(Color.rgb(0, 255, 29, 0.5));
                    case 4 -> rect.setFill(Color.rgb(0, 13, 255, 0.5));
                    case 9 -> rect.setFill(Color.rgb(136, 72, 0, 0.5));
                    default -> rect.setFill(Color.rgb(255, 255, 255, 0.5));
                }
                root.getChildren().add(rect);
            }
        }
        // Crear el triángulo para representar al jugador
        playerTriangle.getPoints().addAll(new Double[]{
                0.0, 0.0,    // Punto 1 (punta del triángulo)
                -5.0, 10.0,  // Punto 2
                5.0, 10.0    // Punto 3
        });
        playerTriangle.setFill(Color.LIGHTBLUE);

        root.getChildren().add(playerTriangle);
    }
    private void updatePlayerPosition() {
        double dx = 0, dy = 0;
        playerIsMoving = false;

        // Calcular la dirección de avance y retroceso
        if (input.contains("W")) {
            dx += Math.cos(Math.toRadians(playerDirection)) * MOVE_SPEED;
            dy += Math.sin(Math.toRadians(playerDirection)) * MOVE_SPEED;
            playerIsMoving = true;
        }
        if (input.contains("S")) {
            dx -= Math.cos(Math.toRadians(playerDirection)) * MOVE_SPEED;
            dy -= Math.sin(Math.toRadians(playerDirection)) * MOVE_SPEED;
            playerIsMoving = true;
        }

        // Calcular la dirección de movimiento lateral (strafe)
        if (input.contains("A")) {
            dx += Math.cos(Math.toRadians(playerDirection - 90)) * MOVE_SPEED;
            dy += Math.sin(Math.toRadians(playerDirection - 90)) * MOVE_SPEED;
            playerIsMoving = true;
        }
        if (input.contains("D")) {
            dx += Math.cos(Math.toRadians(playerDirection + 90)) * MOVE_SPEED;
            dy += Math.sin(Math.toRadians(playerDirection + 90)) * MOVE_SPEED;
            playerIsMoving = true;
        }

        // Calcular la posición central del triángulo
        double centerX = playerPosX * TILE_SIZE;
        double centerY = playerPosY * TILE_SIZE-3;

        // Ajustar la posición del triángulo
        playerTriangle.setTranslateX(centerX + 1.5 );
        playerTriangle.setTranslateY(centerY - 1.5 );

        // Establecer el punto de pivote para la rotación en el centro del triángulo
        playerTriangle.setRotate(playerDirection + 90); // Restar 90 grados si el triángulo apunta hacia arriba inicialmente

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
    // Método para dibujar los muros
    private void drawWalls() {
        screenLayer.getChildren().clear(); // Limpia la capa de pantalla para las líneas verticales
        // Dibujar el cielo como un rectángulo
        Rectangle sky = new Rectangle(0, 0, SCREEN_WIDTH, (double) SCREEN_HEIGHT);
        sky.setFill(Color.ORANGE); // Color azul marino para el cielo
        screenLayer.getChildren().add(sky);

        double angleStep = FOV / SCREEN_WIDTH;
        double startAngle = playerDirection - FOV / 2;

        for (int i = 0; i < SCREEN_WIDTH; i++) {
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
            //Voltear arriba y abajo con el mouse
            double lineTop = ((MID_VERTICAL_SCREEN * 2) - lineHeight) / 2 + VISION_ANGLE;
            double lineBottom = lineTop + lineHeight;

            // Dibuja el piso
            drawFloor(i,lineBottom);

            // Ajustar el color basándose en la distancia
            double brightness = 1.0 - Math.min(1.0, correctedRayLength / MAX_RAY_DISTANCE);
            Color wallColor = Color.WHITE;
            switch (hitWall){
                case 1 -> wallColor = Color.BLUE;
                case 2 -> wallColor = Color.RED;
                case 3 -> wallColor = Color.GREEN;
                case 4 -> wallColor = Color.PURPLE;
                case 9 -> {
                    wallColor = getRandomColor();
                    //hitTexturedWall = true;
                }
            }

            wallColor = wallColor.deriveColor(0, 1, brightness, 1);

            if (hitTexturedWall) {
                // Aquí deberías calcular qué parte de la textura mapear y luego dibujarla.
                // Este es un lugar donde necesitarás una lógica más compleja para mapear correctamente la textura.
                System.out.println("Muro con textura ");
            } else {
                Line screenLine = new Line(i, lineTop, i, lineBottom);
                screenLine.setStroke(wallColor); // Color para muros no texturizados
                screenLayer.getChildren().add(screenLine);
            }
        }
    }
    // Método para generar un color aleatorio
    public static Color getRandomColor() {
        Random random = new Random();

        double red = random.nextDouble();
        double green = random.nextDouble();
        double blue = random.nextDouble();

        return new Color(red, green, blue, 1.0);
    }
    //Metodo para dibujar el piso
    private void drawFloor(int i, double lineBottom) {
        int x,y;
        int xo=400;
        int yo=300;
        float fov = 200;

        Line floorLine = new Line(i, lineBottom, i, SCREEN_HEIGHT);
        floorLine.setStroke(Color.NAVY);
        screenLayer.getChildren().add(floorLine);
    }
    // Método para dibujar los rayos en el mini mapa
    private void castRays() {
        // Eliminar los rayos anteriores
        ((Pane) player.getParent()).getChildren().removeIf(node -> node instanceof Line);

        double angleStep = FOV / SCREEN_WIDTH;
        double startAngle = playerDirection - FOV / 2;

        for (int i = 0; i < SCREEN_WIDTH; i++) {
            // Comment to display all the rays
            if (i == SCREEN_WIDTH / 2) {
                double rayAngle = startAngle + i * angleStep;
                double rayLength = 0;

                while (true) {
                    double checkX = playerPosX + rayLength * Math.cos(Math.toRadians(rayAngle));
                    double checkY = playerPosY + rayLength * Math.sin(Math.toRadians(rayAngle));

                    int mapX = (int) checkX;
                    int mapY = (int) checkY;

                    if (mapX < 0 || mapX >= MAP[0].length || mapY < 0 || mapY >= MAP.length || MAP[mapY][mapX] > 0) {
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
    }
    private void updateSwordPosition() {
        if (playerIsMoving) {
            if (movingUp) {
                swordYOffset -= SWORD_MOVEMENT_SPEED;
                if (swordYOffset < -5) movingUp = false; // Ajusta el rango de movimiento
            } else {
                swordYOffset += SWORD_MOVEMENT_SPEED;
                if (swordYOffset > 5) movingUp = true; // Ajusta el rango de movimiento
            }
            weaponImage.setY(weaponImage.getY() + swordYOffset);
        }
    }
    private void loadWeaponImage() {
        Image image = new Image("/gun.gif"); // Asegúrate de que la ruta sea correcta
        weaponImage = new ImageView(image);
        weaponImage.setX(SCREEN_WIDTH - (SCREEN_WIDTH * 0.2)); // Establece la posición inicial X
        weaponImage.setY(SCREEN_HEIGHT - (SCREEN_HEIGHT * 0.2)); // Establece la posición inicial Y
        weaponImage.setFitHeight(400);
        weaponImage.setFitWidth(500);
    }
    // Launch the app
    public static void main(String[] args) {
        launch(args);
    }
}
