package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.utils.WorldContactListener.*;

/**
 * Created by Fionn on 22/01/2017.
 */
public class Barrier extends B2DSprite{

    private GameScreen screen;
    private Hero hero;
    private float shieldSize;
    private boolean raised;

    public Barrier(GameScreen screen, Hero hero){
        super();
        this.screen = screen;
        this.hero = hero;
        this.shieldSize = 10.5f;
        setToDestroy = false;
        raised = false;
        defineShield(hero.getPosition());
        b2body.setActive(false);
    }

    public void update(float dt) {
        b2body.setTransform(hero.getPosition(), hero.angleToCursor());
        if(raised){
            hero.drain(5 * dt);
        }
    }

    public void raise(){
        b2body.setActive(true);
        raised = true;
    }

    public void lower(){
        b2body.setActive(false);
        raised = false;
    }

    private void defineShield(Vector2 position) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(position.x, position.y);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        b2body = screen.getWorld().createBody(bdef);

        // create the points for the barrier
        // points made relative to body

        float rotation = 0.0f;
        float angleOffset = MathUtils.PI / 4;

        float px = (MathUtils.cos(rotation) * (shieldSize)) / PPM;
        float py = (MathUtils.sin(rotation) * (shieldSize)) / PPM;
        Vector2 p1 = new Vector2(px, py);

        px = (MathUtils.cos(rotation - angleOffset) * (shieldSize)) / PPM;
        py = (MathUtils.sin(rotation - angleOffset) * (shieldSize)) / PPM;
        Vector2 p2 = new Vector2(px, py);

        px = (MathUtils.cos(rotation + angleOffset) * (shieldSize)) / PPM;
        py = (MathUtils.sin(rotation + angleOffset) * (shieldSize)) / PPM;
        Vector2 p3 = new Vector2(px, py);

        EdgeShape shape = new EdgeShape();
        shape.set(p1, p2);
        shape.setRadius(0.5f / PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.filter.categoryBits = BARRIER;
        fdef.filter.maskBits = PLAYER_PROJECTILE | ENEMY_PROJECTILE | ENEMY;

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
        raised = false;
    }
}
