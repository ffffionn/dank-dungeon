package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.utils.WorldContactListener.*;

/**
 * Created by Fionn on 20/11/2016.
 */
public class Projectile extends B2DSprite {

    protected int damageAmount;
    protected float speed;
    protected Vector2 velocity;
    protected GameScreen screen;

    public Projectile(GameScreen screen, Vector2 startPosition, Vector2 target){
        super();
        this.screen = screen;
        this.speed = 1.75f;
        this.damageAmount = 20;
        defineProjectile(startPosition);
        velocity = target.cpy().sub(startPosition).nor().scl(speed);
        b2body.setLinearVelocity(velocity);
    }

    public Projectile(GameScreen screen, Vector2 startPosition, Vector2 target, int damage, float s){
        this(screen, startPosition, target);
        this.damageAmount = damage;
        this.speed = s;
        // recalculate velocity
        velocity = target.cpy().sub(startPosition).nor().scl(speed);
        b2body.setLinearVelocity(velocity);
    }

    public void update(float delta) {
        if( setToDestroy && !destroyed ){
            destroyed = true;
        }
    }

    protected void defineProjectile(Vector2 startPosition){
        BodyDef bdef = new BodyDef();
        bdef.position.set(startPosition);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = true;
        bdef.linearDamping = 0.0f;
        this.b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(2.8f / PPM);
        fdef.shape = shape;
        fdef.friction = 0.0f;
        fdef.restitution = 0.0f;
        fdef.isSensor = true;

        fdef.filter.maskBits = WALL | BARRIER;

        // is it friendly?
        if(startPosition == screen.getPlayer().getPosition()){
            fdef.filter.categoryBits = PLAYER_PROJECTILE;
            fdef.filter.maskBits |= ENEMY;
            b2body.createFixture(fdef).setUserData("player-fireball");
        }else{
            fdef.filter.categoryBits = ENEMY_PROJECTILE;
            fdef.filter.maskBits |= PLAYER;
            b2body.createFixture(fdef).setUserData("enemy-fireball");
        }

        b2body.setUserData(this);
    }

    public int getDamageAmount(){ return this.damageAmount; }

    private boolean isFriendly(){
        return true;
    }
}
