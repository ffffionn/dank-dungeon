package com.test.test.models;

import static com.test.test.SpaceAnts.PPM;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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

    private float modifier;
    private static float MAX_VELOCITY = 2.5f;
    private static int MAX_FIREBALLS = 3;

    // animation frames
    TextureRegion[] moveAnimation;
    TextureRegion[] standAnimation;
    TextureRegion[] castAnimation;
    TextureRegion[] dieAnimation;


    public Hero(GameScreen screen, Vector2 position){
        super();
        this.screen = screen;
        currentState = State.STANDING;
        previousState = State.STANDING;
        fireballs = new Array<Fireball>(MAX_FIREBALLS);
        modifier = 1.0f;
        health = 100;

        define(position);

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

    private void define(Vector2 position){
        BodyDef bdef = new BodyDef();
//        System.out.printf("X: %d  Y: %d  \n", position.x, position.y);
        bdef.position.set((position.x + 0.5f) * 20/ PPM, (position.y + 0.5f) * 20 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(7 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

        b2body.createFixture(fdef).setUserData("player");
        b2body.setUserData(this);
    }

    public void redefine(Vector2 position){
//        for(Fireball f : fireballs){
//            screen.getWorld().destroyBody(f.getBody());
//        }
        System.out.printf("%d FIRE \n", fireballs.size);
        fireballs.clear();
        screen.getWorld().destroyBody(b2body);
        define(position);
    }

    @Override
    public void setTexture(TextureRegion texture){
//        sprite.setBounds(0, 0, texture.getRegionWidth() / PPM, texture.getRegionHeight() / PPM);
        sprite.setBounds(0, 0, 16 / PPM, 16 / PPM);
        sprite.setOriginCenter();
        sprite.setRegion(texture);
    }

    public void update(float dt){
        if( currentState != State.DEAD){
            handleInput(dt);

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
        }else{
            // set game over
            if( animation.getTimesPlayed() > 0 ){
                setToDestroy();
            }
        }

        sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                b2body.getPosition().y - sprite.getHeight() / 2);
        animation.update(dt);

        for(Fireball fb : fireballs){
            if (fb.isDestroyed()){
                fireballs.removeValue(fb, true);
            }else{
                fb.update(dt);
            }
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

    public void shoot(){
        Fireball fb = new Fireball(screen, this);
        screen.add(fb);
        fireballs.add(fb);
    }

    public boolean isDead(){
        return currentState == State.DEAD;
    }

    public void damage(int hp){
        this.health -= hp;
        if (health < 0){
            die();
        }
        screen.getHud().updatePlayerHealth(this.health);
    }

    private void die(){
        changeState(State.DEAD);
        setAnimation(dieAnimation, 1/12f);
    }

    private void handleInput(float dt){
        if (Gdx.input.isKeyPressed(Input.Keys.W) && b2body.getLinearVelocity().y <= MAX_VELOCITY) {
            b2body.applyLinearImpulse(new Vector2(0, 0.2f * modifier), b2body.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && b2body.getLinearVelocity().x <= MAX_VELOCITY) {
            b2body.applyLinearImpulse(new Vector2(0.2f * modifier, 0), b2body.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && b2body.getLinearVelocity().x >= -MAX_VELOCITY) {
            b2body.applyLinearImpulse(new Vector2(-0.2f * modifier, 0), b2body.getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && b2body.getLinearVelocity().y >= -MAX_VELOCITY) {
            b2body.applyLinearImpulse(new Vector2(0, -0.2f * modifier), b2body.getWorldCenter(), true);
        }
        if (Gdx.input.justTouched()) shoot();

        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            modifier = 3.0f;
        }else{
            modifier = 1.0f;
        }
    }
}
