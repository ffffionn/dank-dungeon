package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 20/11/2016.
 */
public class Fireball extends B2DSprite {

    private Vector2 position, velocity;
    private static final float BULLET_SPEED = 1.5f;
    private GameScreen screen;

    public Fireball(GameScreen screen, Hero player){
        super();
        this.screen = screen;
        defineFireball(player.getPosition());
        Vector2 target = screen.getCursor().getPosition();
        position = player.getPosition();
        velocity = target.cpy().sub(position).nor().scl(BULLET_SPEED);
        b2body.setLinearVelocity(velocity);
    }

    public void update(float delta) {
        if( setToDestroy && !destroyed ){
            destroyed = true;
        }
    }

    private void defineFireball(Vector2 heroPosition){
        BodyDef bdef = new BodyDef();
        bdef.position.set(heroPosition);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = true;
        bdef.linearDamping = 0.0f;
        this.b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(2 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.0f;
        fdef.restitution = 0.0f;
        fdef.isSensor = true;

        b2body.createFixture(fdef).setUserData("fireball");
        b2body.setUserData(this);
    }

}
