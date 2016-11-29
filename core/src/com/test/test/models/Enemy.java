package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 20/11/2016.
 */
public class Enemy extends B2DSprite {

    protected static final float MAX_SPEED = 0.75f;

    protected GameScreen screen;
    protected Vector2 target, velocity;


    public Enemy(GameScreen screen, Vector2 startPosition){
        super();
        this.screen = screen;
        define(startPosition);
        target = b2body.getPosition();
    }

    public void update(float dt){
        if( setToDestroy && !destroyed ){
            destroyed = true;
//            screen.delete(this);
        }else if( !destroyed ){
            velocity = target.cpy().sub(b2body.getPosition()).nor().scl(MAX_SPEED);
            b2body.setLinearVelocity(velocity);
            // sprite stuff
        }
    }

    private void define(Vector2 startPoint){

        BodyDef bdef = new BodyDef();
        bdef.position.set((startPoint.x + 0.5f) * 20 / PPM, startPoint.y * 20 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 5.0f;
        bdef.fixedRotation = true;
        this.b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(5 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

        b2body.createFixture(fdef).setUserData("enemy");
        b2body.setUserData(this);
    }

    public void setTarget(Vector2 target){
        this.target = target;
    }

}
