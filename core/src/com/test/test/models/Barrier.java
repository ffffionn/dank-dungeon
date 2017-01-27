package com.test.test.models;

import com.badlogic.gdx.Game;
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
        if( setToDestroy && !destroyed ){
            System.out.println("destroy set");
            destroyed = true;
        }
//        rotateShield(rotation);
        b2body.setTransform(hero.getPosition(), rotation);
    }

    private void rotateShield(float rotation){
        System.out.printf("rotating %f \n", rotation);
        b2body.setTransform(b2body.getPosition(), rotation);
//        b2body.get
    }

    public void raise(float initialRotation){
        defineShield(hero.getPosition(), initialRotation);
        setToDestroy = false;
        screen.add(this);
    }

    private void defineShield(Vector2 position, float rotation) {
        System.out.printf("creating shield at %s  - hero is %s  \n", position.toString(), hero.getPosition());
        BodyDef bdef = new BodyDef();
        bdef.position.set(position.x, position.y);
        bdef.type = BodyDef.BodyType.KinematicBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        b2body = screen.getWorld().createBody(bdef);

        Vector2 p1 = new Vector2(position.x , position.y + (0.1f / PPM));
        Vector2 p2 = new Vector2(position.x + (0.5f / PPM), position.y + (0.05f / PPM));
        Vector2 p3 = new Vector2(position.x - (0.5f / PPM), position.y + (0.05f / PPM));

        System.out.printf("Barrier points at: <%s> - <%s> - <%s>  \n", p1.toString(), p2.toString(), p3.toString());

//        Vector2 p1 = new Vector2((position.x) * 20 / PPM, (position.y) * 20 / PPM);
//        Vector2 p2 = new Vector2((position.x + 0.03f) * 20 / PPM, (position.y - 0.7f) * 20 / PPM);
//        Vector2 p3 = new Vector2((position.x - 0.03f) * 20 / PPM, (position.y - 0.7f) * 20 / PPM);

        FixtureDef fdef = new FixtureDef();
        EdgeShape shape = new EdgeShape();
        shape.set(p1, p2);
        shape.setRadius(1.0f / PPM);

        fdef.shape = shape;
        fdef.isSensor = true;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;
        b2body.createFixture(fdef).setUserData("barrier");

        //create other edge
        shape.set(p1, p3);
        fdef.shape = shape;

        b2body.createFixture(fdef).setUserData("barrier");

        b2body.setUserData(this);
    }

}
