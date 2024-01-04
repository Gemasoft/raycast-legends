package com.gemasoft.gema_engine;

import javafx.scene.image.Image;

class Sprite {
    double x;
    double y;
    Image image; // Imagen del sprite

    public Sprite(double x, double y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    // Métodos para obtener la distancia y ángulo al jugador, dibujar el sprite, etc.
}
