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
    private World world;
    private Array<Fireball> fireballs;
    private GameScreen screen;

    public Hero(Body body, World world, GameScreen screen){
        super(body);
        this.screen = screen;
        this.world = world;
        currentState = State.STANDING;
        previousState = State.STANDING;
        fireballs = new Array<Fireball>();

    }

    @Override
    public void setTexture(TextureRegion texture){
//        sprite.setBounds(0, 0, texture.getRegionWidth() / PPM, texture.getRegionHeight() / PPM);
        sprite.setBounds(0, 0, 16 / PPM, 16 / PPM);
        sprite.setOriginCenter();
        sprite.setRegion(texture);
    }

    public void update(float dt){
        if( b2body.getLinearVelocity().x == 0 && b2body.getLinearVelocity().y == 0 ){
            currentState = State.STANDING;
        }else{
            currentState = State.MOVING;
        }
//        System.out.println(currentState.toString());
        sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                b2body.getPosition().y - sprite.getHeight() / 2);
        animation.update(dt);
        for(Fireball fb : fireballs){
            fb.update(dt);
        }
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

        Body b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(2 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.0f;
        fdef.restitution = 0.0f;
        // check against enemy/wall bits
        fdef.isSensor = true;

        b2body.createFixture(fdef);

        Fireball fireball = new Fireball(b2body, screen.getCursor().getPosition());
        fireballs.add(fireball);
        return fireball;
    }


}
