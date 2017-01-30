package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 22/01/2017.
 */
public class Barrier extends B2DSprite{

    private GameScreen screen;
    private Hero hero;

    public Barrier(GameScreen screen, Hero hero){
        super();
        this.screen = screen;
        this.hero = hero;
        setToDestroy = false;
    }

    public void update(float rotation) {
        b2body.setTransform(hero.getPosition(), 0);
    }

    private void rotateShield(float rotation){
        b2body.setTransform(b2body.getPosition(), rotation);
    }

    public void raise(float initialRotation){
        defineShield(hero.getPosition(), initialRotation);
        setToDestroy = false;
        screen.add(this);
    }

    private void defineShield(Vector2 position, float rotation) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(position.x, position.y);
//        bdef.angle = rotation;
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        b2body = screen.getWorld().createBody(bdef);

        float px = (MathUtils.cos(rotation) * (9.5f)) / PPM;
        float py = (MathUtils.sin(rotation) * (9.5f)) / PPM;
        float angleOffset = MathUtils.PI / 4;
        Vector2 p1 = new Vector2(px, py);

        px = (MathUtils.cos(rotation - angleOffset) * (9.5f)) / PPM;
        py = (MathUtils.sin(rotation - angleOffset) * (9.5f)) / PPM;
        Vector2 p2 = new Vector2(px, py);

        px = (MathUtils.cos(rotation + angleOffset) * (9.5f)) / PPM;
        py = (MathUtils.sin(rotation + angleOffset) * (9.5f)) / PPM;
        Vector2 p3 = new Vector2(px, py);

        FixtureDef fdef = new FixtureDef();

        EdgeShape shape = new EdgeShape();
        shape.set(p1, p2);
        fdef.shape = shape;
//        fdef.isSensor = true;
        b2body.createFixture(fdef).setUserData("barrier");

        shape.dispose();
        //create other edge
        shape = new EdgeShape();
        shape.set(p1, p3);
        fdef.shape = shape;

        b2body.createFixture(fdef).setUserData("barrier");
        b2body.setUserData(this);
        shape.dispose();
    }

}
