package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 20/11/2016.
 */
public class Enemy extends B2DSprite {

    private static final float MAX_SPEED = 1.2f;
    private boolean walkingLeft;

    public Enemy(Body body){
        super(body);
        walkingLeft = false;
    }

    public void update(float dt){
        super.update(dt);


        if( !walkingLeft && b2body.getLinearVelocity().x < MAX_SPEED){
            b2body.applyLinearImpulse(new Vector2(0.2f, 0f), b2body.getWorldCenter(), true);
            if( b2body.getPosition().x > 240 / PPM){
                walkingLeft = true;
            }
        }

        if( walkingLeft && b2body.getLinearVelocity().x > -MAX_SPEED){
            b2body.applyLinearImpulse(new Vector2(-0.2f, 0f), b2body.getWorldCenter(), true);
            if( b2body.getPosition().x < 50 / PPM){
                walkingLeft = false;
            }
        }
    }

}
