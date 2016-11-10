package com.test.test.models;

import com.badlogic.gdx.Gdx;
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

    private boolean isDead;
    private GameScreen screen;
    private int health;


    private TextureRegion playerTexture;


    public Hero(GameScreen screen, TextureRegion texture){
        this.screen = screen;
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        health = 100;
        isDead = false;
        setBounds(0, 0, 16 / SpaceAnts.PPM, 16 / SpaceAnts.PPM);
        setRegion(texture);

        define();
    }

    public void define(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(32 / SpaceAnts.PPM, 32 / SpaceAnts.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = false;
        bdef.linearDamping = 3.0f;
        this.b2body = world.createBody(bdef);

//        bdef = new BodyDef();
//        bdef.type = BodyDef.BodyType.KinematicBody;
//        bdef.position.set(32 / SpaceAnts.PPM, 32 / SpaceAnts.PPM);
//
//        this.cursorBody = world.createBody(bdef);
//
//        WheelJointDef jointDef = new WheelJointDef();
//        jointDef.maxMotorTorque = 0f;
//        jointDef.frequencyHz = 0f;
//        jointDef.motorSpeed = 0f;
//        jointDef.dampingRatio = 0f;
//        jointDef.initialize(b2body, cursorBody, new Vector2(1, 1), new Vector2(1, 1));
//
//        WheelJoint joint = (WheelJoint) world.createJoint(jointDef);
//        joint.setMotorSpeed(1f);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(10 / SpaceAnts.PPM);
        fdef.shape = shape;
//        fdef.density = 5.0f;
        fdef.friction = 0.5f;
        fdef.restitution = 0.1f;

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
        System.out.println("batchin");
        super.draw(batch);
    }



}
