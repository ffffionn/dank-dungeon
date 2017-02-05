package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 20/11/2016.
 */
public class Enemy extends B2DSprite {

    protected static final float MAX_SPEED = 0.75f;
    protected static final int SCORE_VALUE = 20;

    protected boolean stunned;

    protected GameScreen screen;
    protected Vector2 target, velocity;


    public Enemy(GameScreen screen, Vector2 startPosition){
        super();
        this.screen = screen;
        define(startPosition);
        target = b2body.getPosition();
        stunned = false;
    }

    public void update(float dt){
        if( setToDestroy && !destroyed ){
            destroyed = true;
        }else if( !destroyed && !stunned ){
            move();
            // sprite stuff
        }
    }

    public void stun(){
        if(!stunned){
            stunned = true;
            System.out.println("STUN!");
            System.out.println(Thread.currentThread().toString());

            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    stunned = false;
                    System.out.println(Thread.currentThread().toString());
                }
            }, 1.0f);
        }
    }

    protected void move(){
        // move towards player position
        velocity = target.cpy().sub(b2body.getPosition()).nor().scl(MAX_SPEED);
        b2body.setLinearVelocity(velocity);
    }

    protected void define(Vector2 startPoint){

        BodyDef bdef = new BodyDef();
        bdef.position.set((startPoint.x + 0.5f) * 20 / PPM, startPoint.y * 20 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 6.0f;
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

    public Vector2 getPosition(){
        return b2body.getPosition();
    }

    public int getScoreValue(){
        return SCORE_VALUE;
    }

}
