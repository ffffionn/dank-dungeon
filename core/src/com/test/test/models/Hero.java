package com.test.test.models;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.screens.GameScreen;

/**
 * Created by Fionn on 22/10/2016.
 */
public class Hero extends Sprite {
    public enum State { STANDING, MOVING, BLOCKING, ATTACKING, DEAD }
    public State currentState;
    public State previousState;

    public Body b2body;
    public World world;

    private boolean isDead;
    private GameScreen screen;
    private int health;


    private TextureRegion playerTexture;


    public Hero(GameScreen screen){
        this.screen = screen;
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        health = 100;
        isDead = false;

        define();
    }

    public void define(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(32, 32);
        bdef.type = BodyDef.BodyType.DynamicBody;
        this.b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(10);
        fdef.shape = shape;

        b2body.createFixture(fdef);
    }

    public void update(float dt){
        setPosition(b2body.getPosition().x - getWidth() / 2,
                    b2body.getPosition().y - getHeight() / 2);
    }

    public boolean isDead(){
        return this.isDead;
    }

    public void draw(Batch batch){
        super.draw(batch);
    }

}
