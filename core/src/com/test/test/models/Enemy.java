package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.utils.WorldContactListener.*;

/**
 * Basic Enemy class.
 */
public class Enemy extends B2DSprite {

    protected GameScreen screen;
    protected Vector2 target, velocity;

    // enemy attributes
    protected float max_speed;
    protected int score_value;
    protected boolean stunned;
    protected float radius = 5.5f;
    protected int attackDamage;

    public Enemy(GameScreen screen, Vector2 startPosition){
        super();
        this.screen = screen;
        define(startPosition);
        this.target = b2body.getPosition();
        this.stunned = false;

        // attribute defaults
        this.health = 100;
        this.max_speed =  0.75f;
        this.score_value = 20;
        this.attackDamage = 5;
    }

    public Enemy(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    public void update(float dt){
        if( setToDestroy && !destroyed ){
            destroyed = true;
        }else if( !destroyed && !stunned ){
            move();
            // sprite stuff
        }
    }

    public void stun(float stunDuration){
        if(!stunned){
            System.out.println("STUN!");
            stunned = true;
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    stunned = false;
                }
            }, stunDuration);
        }
    }

    /**
     * The Enemy's movement patterns, ie. the AI
     */
    protected void move(){
        // move towards player position
        Vector2 currentPosition = b2body.getPosition();
        float angleToPlayer = MathUtils.atan2((target.y - currentPosition.y), (target.x - currentPosition.x));
        velocity = target.cpy().sub(currentPosition).nor().scl(max_speed);
        b2body.setLinearVelocity(velocity);
        b2body.setTransform(currentPosition, angleToPlayer);
    }

    protected void moveTowards(Vector2 position){
        // move towards player position
        Vector2 currentPosition = b2body.getPosition();
        float angleToPlayer = MathUtils.atan2((position.y - currentPosition.y), (position.x - currentPosition.x));
        velocity = position.cpy().sub(currentPosition).nor().scl(max_speed);
        b2body.setLinearVelocity(velocity);
        b2body.setTransform(currentPosition, angleToPlayer);
    }


    protected void define(Vector2 startPoint){
        BodyDef bdef = new BodyDef();
        bdef.position.set(startPoint);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 6.0f;
        bdef.fixedRotation = true;
        this.b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(getSize());
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;
        fdef.filter.categoryBits = ENEMY;
        fdef.filter.maskBits = WALL | PLAYER | PLAYER_PROJECTILE | BARRIER;

        b2body.createFixture(fdef).setUserData("enemy");
        b2body.setUserData(this);
    }

    public void setTarget(Vector2 target){
        this.target = target;
    }

    public Vector2 getPosition(){
        return b2body.getPosition();
    }

    public int getScoreValue(){
        return this.score_value;
    }
    public int getAttackDamage(){ return this.attackDamage; }

    // Override to change size of subclass bodies with default definition.
    protected float getSize(){ return this.radius / PPM; }


}
