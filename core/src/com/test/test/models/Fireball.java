package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by Fionn on 20/11/2016.
 */
public class Fireball extends B2DSprite {

    private Vector2 position, velocity, acceleration;
    private static final float BULLET_SPEED = 1.5f;

    public Fireball(Body body, Vector2 target){
        super(body);
        position = new Vector2(body.getPosition().x, body.getPosition().y);
        velocity = target.cpy().sub(position).nor().scl(BULLET_SPEED);
        System.out.printf("pos: %s \t target: %s  \t vel: %s \n", position.toString(), target.toString(), velocity.toString());
    }

    public void update(float delta) {
        // simple Euler integration
//        position.add(velocity.cpy().scl(delta));
        b2body.setLinearVelocity(velocity);
    }

}
