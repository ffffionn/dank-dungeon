package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 20/11/2016.
 */
public class Enemy extends B2DSprite {

    protected static final float MAX_SPEED = 0.75f;

    protected GameScreen screen;
    protected Vector2 target, velocity;


    public Enemy(Body body, GameScreen screen){
        super(body);
        this.screen = screen;
        target = b2body.getPosition();
    }

    public Enemy(Body body, GameScreen screen, Vector2 target){
        super(body);
        this.screen = screen;
        this.target = target;
    }

    public void update(float dt){
        super.update(dt);

        if( setToDestroy && !destroyed ){
            screen.getWorld().destroyBody(b2body);
            destroyed = true;
        }else if( !destroyed ){
            velocity = target.cpy().sub(b2body.getPosition()).nor().scl(MAX_SPEED);
            b2body.setLinearVelocity(velocity);
        }

    }


    public void setTarget(Vector2 target){
        this.target = target;
    }
}
