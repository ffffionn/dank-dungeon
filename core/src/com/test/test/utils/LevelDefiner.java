package com.test.test.utils;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.models.Hero;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 20/11/2016.
 */
public class LevelDefiner {

    private GameScreen screen;
    private World world;

    public LevelDefiner(World world, GameScreen screen){
        this.world = world;
        this.screen = screen;
    }

    public Hero defineHero(TextureRegion texture){
        BodyDef bdef = new BodyDef();
        bdef.position.set(60 / PPM, 60 / PPM);
//        bdef.angle = -2.7f;
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 5.0f;
        bdef.fixedRotation = true;
        Body b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(7 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

        b2body.createFixture(fdef);

//        return new Hero(b2body);
        return new Hero(screen, texture);
    }


    public Body defineCursor(){
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(32 / PPM, 32 / PPM);

        CircleShape shape = new CircleShape();
        shape.setRadius(5 / PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 0f;
        fdef.friction = 0f;
        fdef.isSensor = true;
        fdef.restitution = 0f;

        Body cursorBody = world.createBody(bdef);
        cursorBody.createFixture(fdef);
        return cursorBody;
    }

}
