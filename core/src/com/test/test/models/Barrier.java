package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.sun.corba.se.impl.oa.poa.ActiveObjectMap;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 22/01/2017.
 */
public class Barrier extends B2DSprite{

    private GameScreen screen;
    private Hero hero;
    private boolean defined;

    public Barrier(GameScreen screen, Hero hero){
        super();
        this.screen = screen;
        this.hero = hero;
        defined = false;
//        b2body.setActive(true);
//        defineShield(hero.getPosition(), hero.angleToCursor());
    }

    public void update() {
        if(defined){
            b2body.setTransform(hero.getPosition(), hero.angleToCursor());
            if(Gdx.input.isKeyJustPressed(Input.Keys.K)){
                b2body.setActive(!b2body.isActive());
            }
        }
    }

    public void raise(){
        defineShield(hero.getPosition(), hero.angleToCursor());
        setToDestroy = false;
        screen.add(this);
        defined = true;
//        b2body.setActive(true);
    }

    private void defineShield(Vector2 position, float rotation) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(position.x, position.y);
        bdef.angle = rotation;
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        b2body = screen.getWorld().createBody(bdef);

        // create the points for the barrier

        // points made relative to body
        rotation = 0.0f;
        float angleOffset = MathUtils.PI / 4;

        float px = (MathUtils.cos(rotation) * (9.5f)) / PPM;
        float py = (MathUtils.sin(rotation) * (9.5f)) / PPM;
        Vector2 p1 = new Vector2(px, py);

        px = (MathUtils.cos(rotation - angleOffset) * (9.5f)) / PPM;
        py = (MathUtils.sin(rotation - angleOffset) * (9.5f)) / PPM;
        Vector2 p2 = new Vector2(px, py);

        px = (MathUtils.cos(rotation + angleOffset) * (9.5f)) / PPM;
        py = (MathUtils.sin(rotation + angleOffset) * (9.5f)) / PPM;
        Vector2 p3 = new Vector2(px, py);

        EdgeShape shape = new EdgeShape();
        shape.set(p1, p2);
        shape.setRadius(0.5f / PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData("barrier");

        shape.set(p1, p3);
        fdef.shape = shape;

        b2body.createFixture(fdef).setUserData("barrier");
        b2body.setUserData(this);
        shape.dispose();
    }

    @Override
    public void setToDestroy() {
        super.setToDestroy();
        defined = false;
    }
}
