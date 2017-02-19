package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;


/**
 * Wolf Enemy type. Slow and tanky enemy, deals high damage.
 */
public class Wolf extends Enemy {

    public Wolf(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);

        // Wolf attributes
        this.health = 500;
        this.max_speed = 0.35f;
        this.score_value = 200;
        this.attackDamage = 9;
        this.radius = 9.0f;
        this.maxSight = 1.8f;
        this.coneAngle = 55 * MathUtils.degreesToRadians;

        define(startPosition);
    }

    public Wolf(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    @Override
    protected void move() {
        if(targetInSight()){
            moveTowards(target);
        }else{
            walkAround();
            faceDirection(b2body.getLinearVelocity().angleRad());
        }
    }


}
