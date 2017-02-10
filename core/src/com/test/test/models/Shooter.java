package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;

/**
 * Created by Fionn on 09/02/2017.
 */
public class Shooter extends Enemy{


    // Wolf default attributes
    private static final int SHOOTER_HEALTH = 60;
    private static final int SHOOTER_SCORE = 150;
    private static final float SHOOTER_SPEED = 0.55f;
    private static final float RADIUS = 4.5f;

    private boolean canAttack;

    private RayCastCallback callback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if( fixture.getUserData() == null){
                System.out.println("null?");
                return 0;
            }else if (fixture.getUserData().equals("player")) {

//                    System.out.println("player!");
                // move towards player;
//                moveToPlayer();
                shoot(target);
                return 0;
            } else {
                if( fixture.getBody().getUserData() instanceof B2DSprite){
//                    System.out.println("something else");
                    return -1;
                }

//                System.out.println("err..");
                return 0;
            }
        }
    };

    public Shooter(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.health = SHOOTER_HEALTH;
        this.max_speed =  SHOOTER_SPEED;
        this.score_value = SHOOTER_SCORE;
        this.canAttack = true;
    }

    public Shooter(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    @Override
    protected void move() {
        // move randomly around until hero is in range
        screen.getWorld().rayCast(callback, b2body.getPosition(), target);
    }

    private void shoot(Vector2 target){
        if(canAttack){
            this.canAttack = false;
            Projectile p = new Projectile(screen, getPosition(), target);
            screen.add(p);
            // can't shoot again for 1s
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    canAttack = true;
                }
            }, 1f);
        }
    }

    @Override
    protected float getSize() {
        return this.RADIUS / PPM;
    }
}
