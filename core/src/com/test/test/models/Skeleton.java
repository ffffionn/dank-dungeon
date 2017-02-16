package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;

/**
 * Skeleton Enemy type. Uses default Enemy attributes.
 */
public class Skeleton extends Enemy {

    public Skeleton(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        define(startPosition);
    }

}
