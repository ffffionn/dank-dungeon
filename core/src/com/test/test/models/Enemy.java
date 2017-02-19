package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.utils.CaveGenerator.worldPositionToCell;
import static com.test.test.utils.WorldContactListener.*;

/**
 * Basic Enemy class.
 */
public abstract class Enemy extends B2DSprite {

    protected GameScreen screen;
    protected Vector2 target;
    protected boolean[][] floor;

    // enemy attributes
    protected float max_speed;
    protected int score_value;
    protected boolean stunned;
    protected float radius;
    protected int attackDamage;
    protected boolean canAttack;


    protected PlayerSearchCallback callback = new PlayerSearchCallback();
    protected float maxSight;
    protected float coneAngle;



    public Enemy(GameScreen screen, Vector2 startPosition){
        super();
        this.screen = screen;
        this.floor = screen.getLevelMap();
        this.stunned = false;
        this.canAttack = true;

        // attribute defaults
        this.health = 80;
        this.radius = 5.5f;
        this.max_speed =  0.75f;
        this.score_value = 20;
        this.attackDamage = 5;
        this.maxSight = 2.0f;
        this.coneAngle = 60 * MathUtils.degreesToRadians;
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

    /**
     * Re-define the enemy's position in the level.
     * @param newPosition The new world co-ordinates for this enemy.
     */
    public void redefine(Vector2 newPosition){
        screen.getWorld().destroyBody(b2body);
        define(newPosition);
    }

    /**
     * Stun this enemy for a duration.
     * @param stunDuration The amount of seconds to stun for. (1.0f is 1 second)
     */
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
     * The Enemy's movement patterns, ie. the AI.
     */
    protected void move(){
        // move towards player position
        if(targetInSight()){
            moveTowards(target);
        }else{
            walkAround();
            faceDirection(b2body.getLinearVelocity().angleRad());
        }
    }


    protected void moveTowards(Vector2 position){
        // if not going full speed move towards a location
        if( b2body.getLinearVelocity().x < max_speed || b2body.getLinearVelocity().y < max_speed){
            faceDirection(MathUtils.atan2((target.y - b2body.getPosition().y), (target.x - b2body.getPosition().x)));
            b2body.setLinearVelocity(position.sub(b2body.getPosition()).nor().scl(max_speed));
        }
    }

    protected void faceDirection(float angle){
        b2body.setTransform(b2body.getPosition(), angle);
        sprite.setRotation(angle * MathUtils.radiansToDegrees);
    }

    protected float angleToTarget(){
        return MathUtils.atan2((target.y - b2body.getPosition().y), (target.x - b2body.getPosition().x));

    }

    protected boolean targetInSight(){
        float angleToPlayer = target.cpy().sub(b2body.getPosition()).angleRad();
        float boundA = b2body.getAngle() + coneAngle;
        float boundB = b2body.getAngle() - coneAngle;

        if( b2body.getPosition().dst(target) < this.maxSight &&
                (angleToPlayer <= Math.max(boundA, boundB) && angleToPlayer > Math.min(boundA, boundB)) ){
            screen.getWorld().rayCast(callback, b2body.getPosition(), target);
            return callback.playerInSight();
        }else{
            return false;
        }
    }

    protected void walkAround(){
        Vector2 pos = worldPositionToCell(b2body.getPosition());
        avoidWalls(pos);

        Vector2 dir = b2body.getLinearVelocity();
        // if not moving, start moving in a random direction
        if( dir.x == 0 && dir.y == 0){
            dir = new Vector2(MathUtils.randomTriangular(-0.2f, 0.2f), MathUtils.randomTriangular(-0.2f, 0.2f));

        }

        // if no surrounding walls, add a random bit of turning
        int x = Math.round(pos.x);
        int y = Math.round(pos.y);
        if(x == 0 || y == 0 || x == floor.length - 1 || y == floor.length - 1 ||
                (!floor[x+1][y] && !floor[x-1][y] && !floor[x][y-1] && !floor[x][y+1])){
            if(MathUtils.randomBoolean(0.4f)){
                dir.add(MathUtils.randomTriangular(), MathUtils.randomTriangular());
            }
        }

        // continue on in the way you're heading
        if( b2body.getLinearVelocity().x < this.max_speed && b2body.getLinearVelocity().y < this.max_speed) {
            b2body.setLinearVelocity(dir.nor().scl(this.max_speed));
        }
        faceDirection(b2body.getLinearVelocity().angleRad());
    }

    protected void avoidWalls(Vector2 cell){
        //  1/2 UP - 3/4 LEFT - 5/6 DOWN - 7/0 RIGHT
        int direction = Math.round(b2body.getLinearVelocity().angle()) / 45;
        int x = Math.round(cell.x);
        int y = Math.round(cell.y);

        switch(direction){
            case 1: // UP
            case 2:
                if(y == floor.length - 1 || floor[x][y + 1]){
                    b2body.applyLinearImpulse(new Vector2(MathUtils.randomTriangular(-0.2f, 0.2f), -0.5f),
                            b2body.getWorldCenter(), true);
                }
                break;
            case 5: // DOWN
            case 6:
                if(y == 0 || floor[x][y - 1]){
                    b2body.applyLinearImpulse(new Vector2(MathUtils.randomTriangular(-0.2f, 0.2f), 0.5f),
                            b2body.getWorldCenter(), true);
                }
                break;
            case 3: // LEFT
            case 4:
                if(x == 0 || floor[x - 1][y]){
                    b2body.applyLinearImpulse(new Vector2(0.5f, MathUtils.randomTriangular(-0.2f, 0.2f)),
                            b2body.getWorldCenter(), true);
                }
                break;
            case 7: // RIGHT
            case 0:
                if(x == floor.length - 1 || floor[x + 1][y]){
                    b2body.applyLinearImpulse(new Vector2(-0.5f, MathUtils.randomTriangular(-0.2f, 0.2f)),
                            b2body.getWorldCenter(), true);
                }
                break;
        }
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
        fdef.filter.maskBits = WALL | PLAYER | PLAYER_PROJECTILE | BARRIER | ENEMY;

        b2body.createFixture(fdef).setUserData("enemy");
        b2body.setUserData(this);
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
                return -1;
            }
            return fraction;
        }

        public boolean playerInSight() {
            return lastHit == Type.PLAYER;
        }
    }

    protected enum Type{ PLAYER, OTHER, WALL }

}
