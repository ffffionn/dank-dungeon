package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
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

    public Wolf(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.health = WOLF_HEALTH;
        this.max_speed =  WOLF_SPEED;
        this.score_value = WOLF_SCORE;
    }

    public Wolf(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    @Override
    protected void move() {
        // move randomly around until hero is in range
        screen.getWorld().rayCast(new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if( fixture.getUserData() == null){
                    return -1;
                }else if (fixture.getUserData().equals("player")) {
//                    System.out.println("player!");
                    // move towards player;
                    moveToPlayer();
                    return 0;
                } else {
//                    System.out.println("something else");
                    if( fixture.getBody().getUserData() instanceof B2DSprite){
                        return 1;
                    }
                    System.out.println("err..");
                    return -1;
                }
            }
        }, b2body.getPosition(), target);
    }

    private void moveToPlayer(){
        // move towards player position
        Vector2 currentPosition = b2body.getPosition();
        float angleToPlayer = MathUtils.atan2((target.y - currentPosition.y), (target.x - currentPosition.x));
        velocity = target.cpy().sub(currentPosition).nor().scl(max_speed);
        b2body.setLinearVelocity(velocity);
        b2body.setTransform(currentPosition, angleToPlayer);
    }

    @Override
    protected float getSize() {
        return this.RADIUS / PPM;
    }

}
