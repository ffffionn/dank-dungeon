package com.test.test.models;

import static com.test.test.SpaceAnts.PPM;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.test.test.screens.GameScreen;

/**
 * Created by Fionn on 22/10/2016.
 */
public class Hero extends B2DSprite {
    public enum State { STANDING, MOVING, ATTACKING, DEAD }
    public State currentState;
    public State previousState;
    private Array<Fireball> fireballs;
    private GameScreen screen;

    // animation frames
    TextureRegion[] moveAnimation;
    TextureRegion[] standAnimation;
    TextureRegion[] castAnimation;
    TextureRegion[] dieAnimation;


    public Hero(Body body, GameScreen screen){
        super(body);
        this.screen = screen;
        currentState = State.STANDING;
        previousState = State.STANDING;
        fireballs = new Array<Fireball>();

        // define animations
        TextureAtlas.AtlasRegion region;
        region = screen.getAtlas().findRegion("player-move");
        moveAnimation = region.split(64, 64)[0];
        region = screen.getAtlas().findRegion("player-cast");
        castAnimation = region.split(64, 64)[0];
        region = screen.getAtlas().findRegion("player-die");
        dieAnimation = region.split(64, 64)[0];
        region = screen.getAtlas().findRegion("player-strafe");
        standAnimation = region.split(64, 64)[0];

        setTexture(moveAnimation[0]);
        setAnimation(moveAnimation, 1 / 12f);
    }

    @Override
    public void setTexture(TextureRegion texture){
//        sprite.setBounds(0, 0, texture.getRegionWidth() / PPM, texture.getRegionHeight() / PPM);
        sprite.setBounds(0, 0, 16 / PPM, 16 / PPM);
        sprite.setOriginCenter();
        sprite.setRegion(texture);
    }

    public void update(float dt){
        if( Math.abs(b2body.getLinearVelocity().x) < 0.1f && Math.abs(b2body.getLinearVelocity().y) < 0.1f ){
            if( previousState != State.STANDING) {
                changeState(State.STANDING);
            }
            setAnimation(standAnimation, 1/12f);
        }else{
            if( previousState != State.MOVING){
                setAnimation(moveAnimation, 1/12f);
            }
            changeState(State.MOVING);
        }
        sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                b2body.getPosition().y - sprite.getHeight() / 2);
        animation.update(dt);
        for(Fireball fb : fireballs){
            fb.update(dt);
        }
    }

    public void changeState(State s){
        previousState = currentState;
        currentState = s;
    }

    public void render(SpriteBatch batch){
        sprite.setRegion(animation.getFrame());

        // rotate region 90 first for perf.
        sprite.rotate90(true);
        sprite.draw(batch);
    }

    public Fireball shoot(){

        BodyDef bdef = new BodyDef();
        bdef.position.set(b2body.getPosition());
        bdef.fixedRotation = true;
        bdef.linearDamping = 0.0f;
        bdef.type = BodyDef.BodyType.DynamicBody;

        Body b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(2 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.0f;
        fdef.restitution = 0.0f;
        // check against enemy/wall bits
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData("fireball");

        Fireball fireball = new Fireball(b2body, screen);
        b2body.setUserData(fireball);
        fireballs.add(fireball);
        return fireball;
    }


}
