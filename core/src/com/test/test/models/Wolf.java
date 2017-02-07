package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;

/**
 * Created by Fionn on 06/02/2017.
 */
public class Wolf extends Enemy {

    // Wolf default attributes
    private static final int WOLF_HEALTH = 500;
    private static final int WOLF_SCORE = 200;
    private static final float WOLF_SPEED = 0.35f;


    public Wolf(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.health = WOLF_HEALTH;
        this.max_speed =  WOLF_SPEED;
        this.score_value = WOLF_SCORE;
        // need to redo define here
    }

    public Wolf(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    @Override
    protected void move() {
        // move randomly around until hero is in range
    }
}
