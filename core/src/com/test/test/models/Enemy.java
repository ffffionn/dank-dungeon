package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.utils.WorldContactListener.*;

/**
 * Basic Enemy class.
 */
public abstract class Enemy extends B2DSprite {

    protected GameScreen screen;
    protected Vector2 target;

    // enemy attributes
    protected float max_speed;
    protected int score_value;
    protected boolean stunned;
    protected float radius;
    protected int attackDamage;
    protected boolean canAttack;


    protected PlayerSearchCallback callback = new PlayerSearchCallback();
    protected float sightRadius;

    public Enemy(GameScreen screen, Vector2 startPosition){
        super();
        this.screen = screen;
        this.stunned = false;
        this.canAttack = true;

        // attribute defaults
        this.health = 100;
        this.radius = 5.5f;
        this.max_speed =  0.75f;
        this.score_value = 20;
        this.attackDamage = 5;
        this.sightRadius = 2.0f;

        this.target = screen.getPlayer().getPosition();
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
        if(targetInSight()){
            moveTowards(target);
        }
    }

    protected boolean targetInSight(){
        if( b2body.getPosition().dst(target) < this.sightRadius ){
            screen.getWorld().rayCast(callback, b2body.getPosition(), target);
            return callback.playerInSight();
        }else{
            return false;
        }
    }

    protected void moveTowards(Vector2 position){
        // if not going full speed move towards player position
        if( b2body.getLinearVelocity().x < max_speed || b2body.getLinearVelocity().y < max_speed){
            Vector2 currentPosition = b2body.getPosition();
            float angleToPlayer = MathUtils.atan2((position.y - currentPosition.y), (position.x - currentPosition.x));
            faceDirection(angleToPlayer);
            b2body.setLinearVelocity(position.sub(currentPosition).nor().scl(max_speed));
        }
    }

    protected void faceDirection(float angle){
        b2body.setTransform(b2body.getPosition(), angle);
        sprite.setRotation(angle * MathUtils.radiansToDegrees);
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
        shape.setRadius(this.radius / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;
        fdef.filter.categoryBits = ENEMY;
        fdef.filter.maskBits = WALL | PLAYER | PLAYER_PROJECTILE | BARRIER;

        b2body.createFixture(fdef).setUserData("enemy");
        b2body.setUserData(this);
    }

    public void redefine(Vector2 startPoint){
        screen.getWorld().destroyBody(b2body);
        define(startPoint);
    }
    public void setTarget(Vector2 target){this.target = target;}
    public Vector2 getPosition(){ return b2body.getPosition();}
    public int getAttackDamage(){ return this.attackDamage; }
    public int getScoreValue(){ return this.score_value;}

    // custom callback class to see if the player is in sight
    protected class PlayerSearchCallback implements RayCastCallback {
        private Type lastHit;

        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if (fixture.getUserData() == null) {
                lastHit = Type.OTHER;
                System.out.println("null?");
                return -1;
            }else if(fixture.getUserData().equals("wall")){
                lastHit = Type.WALL;
                return 0;
            } else if (fixture.getUserData().equals("player") || fixture.getUserData().equals("barrier")) {
                lastHit = Type.PLAYER;
            } else if (fixture.getBody().getUserData() instanceof Projectile ||
                    fixture.getBody().getUserData() instanceof Enemy) {
                lastHit = Type.OTHER;
                return -1;
            } else {
                lastHit = Type.OTHER;
                return 1;
            }
            return fraction;
        }

        public boolean playerInSight() {
            return lastHit == Type.PLAYER;
        }
    }

    private enum Type{ PLAYER, OTHER, WALL }

}
