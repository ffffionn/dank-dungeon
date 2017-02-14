package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;

/**
 * Created by Fionn on 06/02/2017.
 */
public class Wolf extends Enemy {

    // Wolf default attributes
    private static final int WOLF_HEALTH = 500;
    private static final int WOLF_SCORE = 200;
    private static final float WOLF_SPEED = 0.35f;
    private static final float RADIUS = 9.0f;

    private boolean canAttack;

    private RayCastCallback callback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if( fixture.getUserData() == null){
                System.out.println("null?");
                return 0;
            }else if (fixture.getUserData().equals("player")) {
                // move towards player;
                moveTowards(target);
                return 0;
            } else {
                if( fixture.getBody().getUserData() instanceof B2DSprite){
                    return 1;
                }
                return 0;
            }
        }
    };

    public Wolf(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.health = WOLF_HEALTH;
        this.max_speed =  WOLF_SPEED;
        this.score_value = WOLF_SCORE;
        this.canAttack = true;
    }

    public Wolf(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    @Override
    protected void move() {
        // move randomly around until hero is in range
        screen.getWorld().rayCast(callback, b2body.getPosition(), target);
    }

    @Override
    protected float getSize() {
        return this.RADIUS / PPM;
    }

}
