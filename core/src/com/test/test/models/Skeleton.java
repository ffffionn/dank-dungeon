package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;

/**
 * Created by Fionn on 11/01/2017.
 */
public class Skeleton extends Enemy {

    protected static final float MAX_SPEED = 0.75f;
    protected static final int SCORE_VALUE = 25;

    protected GameScreen screen;
    protected Vector2 target, velocity;


    public Skeleton(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.screen = screen;
        define(startPosition);
        target = b2body.getPosition();
    }


}
