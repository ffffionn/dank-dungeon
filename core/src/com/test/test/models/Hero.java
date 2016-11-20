package com.test.test.models;

import static  com.test.test.SpaceAnts.PPM;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.test.test.SpaceAnts;
import com.test.test.screens.GameScreen;

/**
 * Created by Fionn on 22/10/2016.
 */
public class Hero extends Sprite {
    public enum State { STANDING, MOVING, BLOCKING, ATTACKING, DEAD }
    public State currentState;
    public State previousState;

    public Body b2body;
    public Body cursorBody;

    public World world;

    private Animation animation;

    private boolean isDead;
    private GameScreen screen;
    private int health;


    private TextureRegion playerTexture;


    public Hero(GameScreen screen, TextureRegion texture){
        this.screen = screen;
        this.world = screen.getWorld();
//        animation = new Animation()
        currentState = State.STANDING;
        previousState = State.STANDING;
        health = 100;
        isDead = false;
        setBounds(0, 0, 16 / PPM, 16 / PPM);
        setRegion(texture);
        setOriginCenter();

        define();
    }

    public void define(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(60 / PPM, 60 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 5.0f;
        bdef.fixedRotation = true;
        this.b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(7 / PPM);
        fdef.shape = shape;
//        fdef.density = 5.0f;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

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
