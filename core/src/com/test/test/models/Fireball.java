package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.test.test.screens.GameScreen;

/**
 * Created by Fionn on 20/11/2016.
 */
public class Fireball extends B2DSprite {

    private Vector2 position, velocity;
    private static final float BULLET_SPEED = 1.5f;
    private GameScreen screen;

    public Fireball(Body body, GameScreen screen){
        super(body);
        this.screen = screen;
        Vector2 target = screen.getCursor().getPosition();
        position = new Vector2(body.getPosition().x, body.getPosition().y);
        velocity = target.cpy().sub(position).nor().scl(BULLET_SPEED);
                    b2body.setLinearVelocity(velocity);

    }

    public void update(float delta) {
        if( setToDestroy && !destroyed ){
            screen.getWorld().destroyBody(b2body);
            destroyed = true;
        }else if( !destroyed ){
//            b2body.setLinearVelocity(velocity);
        }
    }

}
