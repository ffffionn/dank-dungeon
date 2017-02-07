package com.test.test.models;

import static com.test.test.SpaceAnts.PPM;
import static com.test.test.screens.GameScreen.TILE_SIZE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
public class Hero extends B2DSprite {

    protected HeroState currentState;
    protected HeroState previousState;
    protected Array<Fireball> fireballs;
    protected GameScreen screen;
    protected Barrier shield;

    // hero attributes
    public static final float MAX_VELOCITY = 2.5f;
    public static final int MAX_HEALTH = 100;
    protected static int MAX_FIREBALLS = 5;
    protected static final float INVINCIBILITY_TIMER = 1.5f;
    protected boolean invincible;

    public static final int HERO_SIZE = 24;

    public Hero(GameScreen screen, Vector2 position){
        super();
        this.screen = screen;
        this.currentState = HeroState.standing;
        this.previousState = HeroState.standing;
        this.fireballs = new Array<Fireball>(MAX_FIREBALLS);
        this.health = MAX_HEALTH;
        this.shield = new Barrier(screen, this);
        this.invincible = false;

        define(position);

        // define animations
        HeroState.defineAnimations(screen.getAtlas());
        setTexture(HeroState.standAnimation[0]);
        currentState.enter(this);
    }

    public void redefine(Vector2 position){
        fireballs.clear();
        screen.getWorld().destroyBody(b2body);
        define(position);
    }

    @Override
    public void setTexture(TextureRegion texture){
        sprite.setBounds(0, 0, HERO_SIZE / PPM, HERO_SIZE / PPM);
        sprite.setOriginCenter();
        sprite.setRegion(texture);
    }

    public void update(float dt){
        if(!isDead()){
            faceCursor();
            shield.update();
            currentState.handleInput(this);
        }else{  // player is dead
            // set game over animation
            if( animation.getTimesPlayed() == 1 ){
                System.out.println("game over --------");
                setToDestroy();
            }
        }
        animation.update(dt);

        sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                b2body.getPosition().y - sprite.getHeight() / 2);
        for(Fireball fb : fireballs){
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
        if( currentState != HeroState.blocking){
            System.out.println("ERROR!");
        }
        System.out.println("unblock..");
        changeState(getPreviousState());
        shield.setToDestroy();
    }

    /**
     * Fires a fireball to direction the hero is facing.
     */
    public void shoot(){
        if(fireballs.size < MAX_FIREBALLS){
            Fireball fb = new Fireball(screen, getPosition());
            setAnimation(HeroState.castAnimation2, 1/30f);
            screen.add(fb);
            fireballs.add(fb);
        }
    }

    public boolean isDead(){
        return currentState == HeroState.dead;
    }

    /**
     * Damages the hero by the given amount.
     * @param hp Amount of hero health to reduce
     */
    public void damage(int hp){
        if(!invincible){
            this.health -= hp;
            if (health < 0){
                die();
            }else{
                invincible = true;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        invincible = false;
                        System.out.println("--vulnerable--");
                    }
                }, INVINCIBILITY_TIMER);
            }
            screen.getHud().updatePlayerHealth(this.health);
        }else{
            System.out.println("INVINCIBLE!");
        }
    }

    public HeroState getCurrentState(){
        return this.currentState;
    }

    public HeroState getPreviousState(){
        return this.previousState;
    }

    /**
     * Change the hero's state to a new HeroState.
     * @param s The new HeroState.
     */
    public void changeState(HeroState s){
        previousState = currentState;
        currentState = s;
        s.enter(this);
        System.out.printf("**STATE:\t%s  ->  %s\n", previousState.toString(), currentState.toString());
    }

    /**
     * Draw the hero.
     * @param batch
     */
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
    private void faceCursor(){
        float angle = angleToCursor();
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

        b2body.createFixture(fdef).setUserData("player");
        b2body.setUserData(this);
    }
}
