package com.test.test.models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.utils.WorldContactListener.*;

/**
 * Projectiles fired by scorpions and the player.
 */
public class Projectile extends AnimatedB2DSprite {

    public static final float DEFAULT_SPEED = 1.75f;
    public static final int DEFAULT_DAMAGE = 20;

    protected static TextureRegion[] fireballAnimation;

    protected int damageAmount;
    protected float speed;
    protected Vector2 velocity;
    protected GameScreen screen;

    protected Color tint;
    protected int bounces;


    public Projectile(GameScreen screen, Vector2 startPosition, Vector2 target){
        super();
        this.bounces = 0;
        this.screen = screen;
        this.speed = DEFAULT_SPEED;
        this.damageAmount = DEFAULT_DAMAGE;
        this.tint = Color.valueOf("#efefef");
        defineProjectile(startPosition, target);
        velocity = target.cpy().sub(startPosition).nor().scl(speed);
        b2body.setLinearVelocity(velocity);

        if(fireballAnimation == null){
            fireballAnimation = screen.getAtlas().findRegion("fireball").split(64, 64)[4];
        }
        setTexture(fireballAnimation[0], 12);
        setAnimation(fireballAnimation, 1 / 12f);
    }

    public Projectile(GameScreen screen, Vector2 startPosition, Vector2 target, int damage, float s){
        this(screen, startPosition, target);
        this.damageAmount = damage;
        this.speed = s;
        // recalculate velocity with new speed
        velocity = target.cpy().sub(startPosition).nor().scl(speed);
        b2body.setLinearVelocity(velocity);
    }

    public Projectile(GameScreen screen, Vector2 startPosition, Vector2 target, Color tint){
        this(screen, startPosition, target);
        this.tint = tint;
    }

    public Projectile(GameScreen screen, Vector2 startPosition, Vector2 target, int damage, float s, Color tint){
        this(screen, startPosition, target, damage, s);
        this.tint = tint;
    }

    public void setBounces(int number){
        this.bounces = number;
    }

    public int getBounces() {
        return this.bounces;
    }

    public void bounce(){
        if(bounces == 0){
            setToDestroy();
        }else{
            this.bounces--;
        }
    }

    public void update(float delta) {
        if( setToDestroy && !destroyed ){
            destroyed = true;
        }else{
            animation.update(delta);

            // stay facing the way it's headed
            b2body.setTransform(b2body.getPosition(), b2body.getLinearVelocity().angleRad());
            // keep the sprite in line with the body
            sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                    b2body.getPosition().y - sprite.getHeight() / 2);
            sprite.setRotation(b2body.getAngle() * MathUtils.radiansToDegrees);

            if (Math.abs(b2body.getLinearVelocity().x) < 0.55f &&
                    Math.abs(b2body.getLinearVelocity().y) < 0.55f) {
                System.out.println("too slow");
                setToDestroy();
            }
        }
    }

    protected void defineProjectile(Vector2 startPosition, Vector2 target){
        BodyDef bdef = new BodyDef();
        bdef.position.set(startPosition);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.angle = MathUtils.atan2((target.y - startPosition.y), (target.x - startPosition.x));
        bdef.fixedRotation = true;
        bdef.linearDamping = 0.0f;
        this.b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(2.2f / PPM);
        fdef.shape = shape;
        fdef.friction = 0.0f;
        fdef.restitution = 1.0f;

        fdef.filter.maskBits = WALL | BARRIER;

        // is it friendly?
        if(startPosition == screen.getPlayer().getPosition()){
            fdef.filter.categoryBits = PLAYER_PROJECTILE;
            fdef.filter.maskBits |= ENEMY | ENEMY_PROJECTILE;
            b2body.createFixture(fdef).setUserData("player-fireball");
        }else{
            fdef.filter.categoryBits = ENEMY_PROJECTILE;
            fdef.filter.maskBits |= PLAYER | PLAYER_PROJECTILE;
            b2body.createFixture(fdef).setUserData("enemy-fireball");
        }

        b2body.setUserData(this);
    }

    @Override
    public void render(SpriteBatch sb) {
        sprite.setRegion(animation.getFrame());
        sprite.setColor(tint);
        sprite.draw(sb);
    }

    protected boolean isFriendly(){ return true; }
    public int getDamageAmount(){ return this.damageAmount; }
}
