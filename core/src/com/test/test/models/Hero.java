package com.test.test.models;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.models.HeroState.attacking;
import static com.test.test.models.HeroState.standing;
import static com.test.test.screens.GameScreen.TILE_SIZE;
import static com.test.test.utils.WorldContactListener.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

/**
 * Hero class for the playable character. Current HeroState handles input.
 */
public class Hero extends AnimatedB2DSprite {

    // hero attributes
    public static final float MAX_VELOCITY = 2.5f;
    public static final int MAX_HEALTH = 100;
    private static final float INVINCIBILITY_TIMER = 0.85f;
    private static final int HERO_SIZE = 20;
    private static int MAX_FIREBALLS = 5;
    private boolean invincible;
    private float mana;
    private Color flashColour;

    private HeroState currentState;
    private HeroState previousState;
    private Array<Projectile> fireballs;
    private GameScreen screen;
    private Barrier shield;

    public Hero(GameScreen screen, Vector2 position){
        super();
        this.screen = screen;
        this.currentState = standing;
        this.previousState = standing;
        this.fireballs = new Array<Projectile>(MAX_FIREBALLS);
        this.health = MAX_HEALTH;
        this.invincible = false;
        this.flashColour = Color.RED;
        define(position);
        this.shield = new Barrier(screen, this);
        this.mana = 15.0f;

        // define animations
        HeroState.defineAnimations(screen.getAtlas());
        setTexture(HeroState.standAnimation[0], HERO_SIZE);
        currentState.enter(this);
    }

    public void redefine(Vector2 position){
        fireballs.clear();
        screen.getWorld().destroyBody(b2body);
        define(position);
    }

    public int getMana(){ return MathUtils.floor(mana);}

    public void update(float dt){
        animation.update(dt);
        if(!isDead()){
            rotate(angleToCursor());
            shield.update(dt);
            currentState.update(dt, this);
            if( currentState == standing && mana < 100.0f){
                this.mana += (((StandingState) currentState).getTimeStanding() / 2.0 * dt);
                screen.getHud().updatePlayerMana(MathUtils.floor(this.mana));
            }
        }else{  // player is dead
            // set game over animation
            if( animation.getTimesPlayed() == 1 ){
                System.out.println("game over --------");
                setToDestroy();
            }
        }

        sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                b2body.getPosition().y - sprite.getHeight() / 2);
        for(Projectile fb : fireballs){
            if (fb.isDestroyed()){
                fireballs.removeValue(fb, true);
            }else{
                fb.update(dt);
            }
        }

        // DEBUG: bail
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) screen.dispose();
    }

    public void block(){
        changeState(HeroState.blocking);
        shield.raise();
    }

    public void unblock(){
        changeState(standing);
        shield.lower();
    }

    /**
     * Fires a fireball to direction the hero is facing.
     */
    public void shoot(){
        if(fireballs.size < MAX_FIREBALLS && mana > 1.0f){
            mana -= 0.5f;
            Projectile fb = new Projectile(screen, getPosition(), screen.getCursor().getPosition());
            screen.add(fb);
            fireballs.add(fb);
            changeState(attacking);
        }
    }

    public boolean isDead(){
        return currentState == HeroState.dead;
    }

    public void drain(float amount){
        if( mana < 0 ){
            mana = 0;
        }else{
            this.mana -= amount;
        }

        // update hud
        screen.getHud().updatePlayerMana(MathUtils.floor(this.mana));
    }

    /**
     * Damages the hero by the given amount.
     * @param damageAmount Amount of hero health to reduce
     */
    @Override
    public void damage(int damageAmount){
        if(!invincible && health > 0){
            this.health -= damageAmount;
            if (health <= 0){
                die();
            }else{
                invincible = true;
                sprite.setColor(Color.WHITE.cpy().lerp(Color.GOLD, 0.8f));
                // tint the player/screen red while invincible
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        invincible = false;
                        System.out.println("--vulnerable--");
                        sprite.setColor(Color.WHITE.cpy().lerp(flashColour, 0.0f));
                    }
                }, INVINCIBILITY_TIMER);
            }
            screen.getHud().updatePlayerHealth(this.health);
        }
    }

    public HeroState getCurrentState(){ return this.currentState; }

    public HeroState getPreviousState(){ return this.previousState; }

    /**
     * Change the hero's state to a new HeroState.
     * @param s The new HeroState.
     */
    public void changeState(HeroState s){
        previousState = currentState;
        currentState = s;
        System.out.printf("**STATE:\t%s  ->  %s\n", previousState.toString(), currentState.toString());
        // trigger state change effects
        previousState.leave(this);
        currentState.enter(this);
    }

    /**
     * Draw the hero.
     * @param batch
     */
    @Override
    public void render(SpriteBatch batch){
        sprite.setRegion(animation.getFrame());
        // rotate region 90 first for perf.
        sprite.rotate90(true);
        sprite.draw(batch);
    }

    private void die(){
        changeState(HeroState.dead);
    }

    /**
     * Rotate the Box2D body and sprite to face the cursor.
     */
    private void rotate(float angle){
        b2body.setTransform(b2body.getPosition(), angle);
        sprite.setRotation(angle * MathUtils.radiansToDegrees);
    }

    /**
     * Get the angle (in radians) from the hero to the cursor.
     * @return
     */
    public float angleToCursor(){
        Vector2 mouse = screen.getCursor().getPosition();
        Vector2 center = b2body.getPosition();
        return MathUtils.atan2((mouse.y - center.y), (mouse.x - center.x));
    }

    /**
     * Create the body and fixtures for the Box2D world.
     * @param position World position to place the hero.
     */
    private void define(Vector2 position){
        BodyDef bdef = new BodyDef();
        bdef.position.set((position.x + 0.5f) * TILE_SIZE / PPM, (position.y + 0.5f) * TILE_SIZE / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(HERO_SIZE / 3 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;
        fdef.filter.categoryBits = PLAYER;
        fdef.filter.maskBits = ENEMY_PROJECTILE | ENEMY | WALL | PICKUP;

        b2body.createFixture(fdef).setUserData("player");
        b2body.setUserData(this);
    }
}
