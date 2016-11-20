package com.test.test.models;

import static com.test.test.SpaceAnts.PPM;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.test.test.screens.GameScreen;
import com.test.test.utils.Animation;

/**
 * Created by Fionn on 22/10/2016.
 */
public class Hero  {
    public enum State { STANDING, MOVING, ATTACKING, DEAD }
    public State currentState;
    public State previousState;

    public Body b2body;
    public Body cursorBody;

    public World world;

    private Animation animation;

    private boolean isDead;
    private GameScreen screen;
    private int health;

    public Sprite sprite;

    private TextureRegion playerTexture;
    private TextureAtlas.AtlasRegion moveRegion;


    public Hero(GameScreen screen, TextureRegion texture){
        this.screen = screen;
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        health = 100;
        isDead = false;
        sprite = new Sprite(texture);
        sprite.setBounds(0, 0, 16 / PPM, 16 / PPM);
        sprite.setOriginCenter();
        sprite.rotate90(true);

        TextureAtlas.AtlasRegion region = screen.getAtlas().findRegion("player-move");
        System.out.println(screen.getAtlas().getRegions().toString());
        System.out.println(region.toString());
        TextureRegion[] moveFrames = region.split(64, 64)[0];

        animation = new Animation(moveFrames, 1 / 12f);

//        define();
    }

    public void define(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(60 / PPM, 60 / PPM);
//        bdef.angle = -2.7f;
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 5.0f;
        bdef.fixedRotation = true;
        this.b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(7 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

        b2body.createFixture(fdef);
    }

    public void update(float dt){
        sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                    b2body.getPosition().y - sprite.getHeight() / 2);
        animation.update(dt);
    }

    public boolean isDead(){
        return this.isDead;
    }

    public void draw(Batch batch){
        sprite.setRegion(animation.getFrame());

        // rotate region 90 first for perf.
        sprite.rotate90(true);
        sprite.draw(batch);
    }



}
