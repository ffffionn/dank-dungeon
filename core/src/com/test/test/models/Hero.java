package com.test.test.models;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.models.HeroState.attacking;
import static com.test.test.models.HeroState.standing;
import static com.test.test.utils.WorldContactListener.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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
    public static final int MAX_MANA = 100;
    private static final float INVINCIBILITY_TIMER = 0.85f;
    private static final int HERO_SIZE = 20;
    private boolean invincible;
    private float mana;
    private Color flashColour;

    private Pickup activePower;
    private HeroState currentState;
    private HeroState previousState;
    private Array<Projectile> fireballs;
    private GameScreen screen;
    private Barrier shield;

    Music footsteps;
    Music barrierSound;
    Sound castSound;


    public Hero(GameScreen screen, Vector2 position){
        super();
        this.screen = screen;
        this.activePower = null;
        this.currentState = standing;
        this.previousState = standing;
        this.fireballs = new Array<Projectile>();
        this.health = MAX_HEALTH;
        this.mana = MAX_MANA;
        this.invincible = false;
        this.flashColour = Color.RED;
        define(position);
        this.shield = new Barrier(screen, this);
        castSound = screen.getAssetManager().get("sounds/fire.ogg", Sound.class);
        barrierSound = screen.getAssetManager().get("sounds/barrier.ogg", Music.class);
        barrierSound.setLooping(true);
        footsteps = screen.getAssetManager().get("sounds/steps.wav", Music.class);
        footsteps.setLooping(true);
        footsteps.setVolume(0.4f);

        // define animations
        HeroState.defineAnimations(screen.getAtlas());
        setTexture(HeroState.standAnimation[0], HERO_SIZE);
        currentState.enter(this);
    }

    public void update(float dt){
        animation.update(dt);
        if(!isDead()){
            rotate(angleToCursor());
            shield.update(dt);
            currentState.update(dt, this);
            tickPower(dt);
            if( currentState == standing && mana < 100.0f){
                this.mana += (((StandingState) currentState).getTimeStanding() / 1.5 * dt);
                screen.getHud().updateMana(MathUtils.floor(this.mana));
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

    /**
     * Fires a fireball to direction the hero is facing.
     */
    public void shoot(){
        if(mana < 1.0f) {
            return;
        }
        Array<Projectile> newFireballs = new Array<Projectile>();
        if(activePower == null){
            newFireballs.add(new Projectile(screen, getPosition(), screen.getCursor().getPosition()));
        }else{
            switch(activePower.TYPE){
                case MULTI_FIRE:
                    float a = 15 * MathUtils.degreesToRadians;
                    float dst = getPosition().dst(screen.getCursor().getPosition());

                    float x = getPosition().x + dst * MathUtils.cos(b2body.getAngle() + a);
                    float y = getPosition().y + dst * MathUtils.sin(b2body.getAngle() + a);
                    newFireballs.add(new Projectile(screen, getPosition(), new Vector2(x, y)));

                    x = getPosition().x + dst * MathUtils.cos(b2body.getAngle() - a);
                    y = getPosition().y + dst * MathUtils.sin(b2body.getAngle() - a);
                    newFireballs.add(new Projectile(screen, getPosition(), new Vector2(x, y)));

                    newFireballs.add(new Projectile(screen, getPosition(), screen.getCursor().getPosition()));
                    break;
                case DOUBLE_DMG:
                    newFireballs.add(new Projectile(screen, getPosition(), screen.getCursor().getPosition(),
                            2 * Projectile.DEFAULT_DAMAGE, 1.2f * Projectile.DEFAULT_SPEED, Color.RED));
                    break;
                default:
                    newFireballs.add(new Projectile(screen, getPosition(), screen.getCursor().getPosition()));
            }
        }
        adjustMana(-1.0f);
        screen.add(newFireballs);
        fireballs.addAll(newFireballs);
        changeState(attacking);
        screen.getAssetManager().get("sounds/cast-spell.wav", Sound.class).play(0.3f);
    }

    public void pickup(Pickup p){
        if(p.TYPE != Pickup.Type.POTION){
            activePower = p;
        }
        p.activate(this);
    }

    private void tickPower(float dt){
        if (activePower != null && activePower instanceof Pickup.TimedPickup) {
            activePower.update(dt);
            if(((Pickup.TimedPickup) activePower).isTimeUp()){
                activePower.deactivate();
                activePower = null;
            }
        }
    }

    public boolean isDead(){
        return currentState == HeroState.dead;
    }

    public void adjustMana(float amount){
        this.mana += amount;
        if(mana < 0){
            mana = 0;
        }else if(mana > MAX_MANA){
            mana = MAX_MANA;
        }

        // update hud
        screen.getHud().updateMana(MathUtils.floor(this.mana));
    }

    public void addHealth(int amount){
        this.health += amount;
        if (health > MAX_HEALTH) health = MAX_HEALTH;
        screen.getHud().updateHealth(this.health);
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
            screen.getHud().updateHealth(this.health);
        }
    }

    public void block(){
        changeState(HeroState.blocking);
        shield.raise();
        barrierSound.play();
    }

    public void unblock(){
        changeState(standing);
        shield.lower();
        barrierSound.stop();
    }

    public void redefine(Vector2 position){
        fireballs.clear();
        screen.getWorld().destroyBody(b2body);
        define(position);
    }

    public int getMana(){ return MathUtils.floor(mana);}
    public Pickup getActivePower(){ return this.activePower; }
    public GameScreen getScreen(){ return this.screen; }
    public HeroState getCurrentState(){ return this.currentState; }
    public HeroState getPreviousState(){ return this.previousState; }

    /**
     * Change the hero's state to a new HeroState.
     * @param s The new HeroState.
     */
    public void changeState(HeroState s){
        previousState = currentState;
        currentState = s;
        // trigger state change effects
        previousState.leave(this);
        currentState.enter(this);
//        System.out.printf("**STATE:\t%s  ->  %s\n", previousState.toString(), currentState.toString());
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
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(HERO_SIZE / 3 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.75f;
        fdef.filter.categoryBits = PLAYER;
        fdef.filter.maskBits = ENEMY_PROJECTILE | ENEMY | WALL | PICKUP;

        b2body.createFixture(fdef).setUserData("player");
        b2body.setUserData(this);
    }
}
