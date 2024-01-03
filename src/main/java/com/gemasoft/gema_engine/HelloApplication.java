package com.gemasoft.gema_engine;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.time.TimerAction;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class HelloApplication extends GameApplication {

    private Rectangle square;
    private static final double SPEED = 5;
    private static final int SQUARE_SIZE = 15;
    private static final int ENEMY_SIZE = 20;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private int score = 0;
    private Text scoreText;
    private final List<Rectangle> enemies = new ArrayList<>();
    private Text roundText;
    private int round = 1;
    private TimerAction roundTimer;
    private static final String MOVE_LEFT = "Move Left";
    private static final String MOVE_RIGHT = "Move Right";
    private static final String MOVE_UP = "Move Up";
    private static final String MOVE_DOWN = "Move Down";
    private static final String ROUND_MESSAGE = "La siguiente ronda comienza en ";
    private final Random random = new Random();
    private static final double ENEMY_SPEED = 2.5;
    private static final double SAFE_DISTANCE = 100; // La distancia a la que los enemigos empiezan a huir
    private static final double ENEMY_WANDER_SPEED = 1.0;
    private Text timerText;


    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(WINDOW_WIDTH);
        settings.setHeight(WINDOW_HEIGHT);
        settings.setTitle("Gemasoft 3D Engine");
    }

    @Override
    protected void initGame() {

        //Creamos el cuadrado (jugador principal)
        square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
        square.setX(100); // Posición inicial X
        square.setY(100); // Posición inicial Y

        FXGL.getGameScene().addUINode(square);

        // Crear y añadir enemigos
        createEnemies(1);

        initScoreCounter();

        initTimerDisplay();

    }

    private void initScoreCounter() {
        scoreText = new Text();
        scoreText = FXGL.getUIFactoryService().newText("Score: " + score, Color.GREEN, 20.0);
        scoreText.setTranslateX(WINDOW_WIDTH - 100); // Posición en X
        scoreText.setTranslateY(20); // Posición en Y

        FXGL.getGameScene().addUINode(scoreText);
    }

    @Override
    protected void onUpdate(double tpf) {
        checkCollisions();
        moveEnemies();
        updateTimer();

    }

    private void initTimerDisplay() {
        timerText = FXGL.getUIFactoryService().newText("", Color.BLACK, 20.0);
        timerText.setTranslateX(15); // Ajusta según tus necesidades
        timerText.setTranslateY(50); // Ajusta según tus necesidades
        FXGL.getGameScene().addUINode(timerText);
    }

    private void updateTimer() {
        if (!enemies.isEmpty()) {
            Duration duration = new Duration(1);
            long seconds = (long) duration.toSeconds();
            long absSeconds = Math.abs(seconds);
            String time = String.format(
                    "%02d:%02d",
                    (absSeconds / 3600),
                    ((absSeconds % 3600) / 60)
            );
            timerText.setText("Tiempo: " + time);
        }
    }


    private void moveEnemies() {
        for (Rectangle enemy : enemies) {
            double dx = enemy.getX() - square.getX();
            double dy = enemy.getY() - square.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            double angle;
            if (distance < SAFE_DISTANCE) {
                // Calcular ángulo para huir del jugador
                angle = Math.atan2(dy, dx); // Agregar Math.PI para invertir la dirección
            } else {
                // Movimiento aleatorio
                angle = random.nextDouble() * 2 * Math.PI;
            }

            // Aplicar el movimiento
            enemy.setX(enemy.getX() + Math.cos(angle) * (distance < SAFE_DISTANCE ? ENEMY_SPEED : ENEMY_WANDER_SPEED));
            enemy.setY(enemy.getY() + Math.sin(angle) * (distance < SAFE_DISTANCE ? ENEMY_SPEED : ENEMY_WANDER_SPEED));

            // Mantener a los enemigos dentro de los límites de la ventana
            enemy.setX(Math.max(0, Math.min(enemy.getX(), WINDOW_WIDTH - ENEMY_SIZE)));
            enemy.setY(Math.max(0, Math.min(enemy.getY(), WINDOW_HEIGHT - ENEMY_SIZE)));
        }
    }


    private void checkCollisions() {
        Iterator<Rectangle> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Rectangle enemy = iterator.next();
            if (square.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                iterator.remove();
                FXGL.getGameScene().removeUINode(enemy);
                score++;
                scoreText.setText("Score: " + score);
            }
        }

        if (enemies.isEmpty() && roundTimer == null) {
            startRoundTimer();
        }
    }

    private void createEnemies(int numberOfEnemies) {
        for (int i = 0; i < numberOfEnemies; i++) {
            Rectangle enemy = new Rectangle(ENEMY_SIZE, ENEMY_SIZE, Color.RED);
            enemy.setX(random.nextDouble() * (WINDOW_WIDTH - ENEMY_SIZE));
            enemy.setY(random.nextDouble() * (WINDOW_HEIGHT - ENEMY_SIZE));
            enemies.add(enemy);
            FXGL.getGameScene().addUINode(enemy);
        }
    }

    @Override
    protected void initInput() {
        addAction(MOVE_LEFT, KeyCode.A, () -> movePlayer(-SPEED, 0));
        addAction(MOVE_RIGHT, KeyCode.D, () -> movePlayer(SPEED, 0));
        addAction(MOVE_UP, KeyCode.W, () -> movePlayer(0, -SPEED));
        addAction(MOVE_DOWN, KeyCode.S, () -> movePlayer(0, SPEED));
    }

    private void addAction(String name, KeyCode code, Runnable action) {
        FXGL.getInput().addAction(new UserAction(name) {
            @Override
            protected void onAction() {
                action.run();
            }
        }, code);
    }

    private void movePlayer(double dx, double dy) {
        double newX = Math.max(0, Math.min(square.getX() + dx, WINDOW_WIDTH - SQUARE_SIZE));
        double newY = Math.max(0, Math.min(square.getY() + dy, WINDOW_HEIGHT - SQUARE_SIZE));
        square.setX(newX);
        square.setY(newY);
    }

    private void startRoundTimer() {
        AtomicInteger countdown = new AtomicInteger(5);

        // Crear roundText si aún no existe
        if (roundText == null) {
            roundText = FXGL.getUIFactoryService().newText("", Color.RED, 20.0);
            roundText.setTranslateX(10);
            roundText.setTranslateY(20);
        }
        FXGL.getGameScene().addUINode(roundText);

        // Mostrar el mensaje de inicio de la ronda
        roundText.setText(ROUND_MESSAGE + countdown.get());

        // Configurar y comenzar el temporizador de la ronda
        roundTimer = FXGL.getGameTimer().runAtInterval(() -> {
            int currentCount = countdown.decrementAndGet();
            roundText.setText(ROUND_MESSAGE + currentCount);

            if (currentCount <= 0) {
                roundTimer.expire();
                roundTimer = null;
                FXGL.getGameScene().removeUINode(roundText); // Opcional: Ocultar el texto
                round++;
                createEnemies(round);
            }
        }, Duration.seconds(1));
    }
    public static void main(String[] args) {
        launch(args);
    }
}