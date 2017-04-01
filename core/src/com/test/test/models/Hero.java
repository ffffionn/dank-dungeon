package com.test.test.models;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.models.HeroState.attacking;
import static com.test.test.models.HeroState.standing;
import static com.test.test.utils.WorldContactListener.*;

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
    public int maxHP;
    public float maxMana;
    private float mana;

    public static final int HERO_SIZE = 20;
    private static final float MAX_SPEED_LIMIT = 7.0f;
    private static final float INVINCIBILITY_TIMER = 0.85f;
    private boolean invincible;
    private int attackDamage;
    private Color flashColour;

    private float maxSpeed;

    private Array<Pickup> activePowers;

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
        this.activePowers = new Array<Pickup>();
        this.currentState = standing;
        this.previousState = standing;
        this.fireballs = new Array<Projectile>();
        this.invincible = false;
        this.flashColour = Color.RED;

        // defaults
        this.health = this.maxHP = 100;
        this.mana = this.maxMana = 100.0f;
        this.attackDamage = 20;
        this.maxSpeed = 2.0f;


        define(position);
        this.shield = new Barrier(screen, this);
        castSound = screen.getAssetManager().get("sounds/cast-spell.wav", Sound.class);
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
                this.mana += (((StandingState) currentState).getTimeStanding() * dt);
                screen.getHud().updateMana(MathUtils.floor(this.mana));
            }
        }else{  // player is dead
            // set game over animation
            if( animation.getTimesPlayed() == 1 ){
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

    }

    /**
     * Fires a fireball to direction the hero is facing.
     */
    public void shoot(){
        if(mana < 1.0f) {
            return;
        }
        Array<Projectile> newFireballs = new Array<Projectile>();

        int damage = attackDamage;
        float speed = 1.2f * Projectile.DEFAULT_SPEED;

        if(hasPower(Pickup.Type.DOUBLE_DMG)){
            damage *= 2;
        }

        newFireballs.add(new Projectile(screen, getPosition(), screen.getCursor().getPosition(), damage, speed));
        if(hasPower(Pickup.Type.MULTI_FIRE)){
            float a = 15 * MathUtils.degreesToRadians;
            float dst = getPosition().dst(screen.getCursor().getPosition());

            float x = getPosition().x + dst * MathUtils.cos(b2body.getAngle() + a);
            float y = getPosition().y + dst * MathUtils.sin(b2body.getAngle() + a);
            newFireballs.add(new Projectile(screen, getPosition(), new Vector2(x, y)));

            x = getPosition().x + dst * MathUtils.cos(b2body.getAngle() - a);
            y = getPosition().y + dst * MathUtils.sin(b2body.getAngle() - a);
            newFireballs.add(new Projectile(screen, getPosition(), new Vector2(x, y), damage, speed));

        }
        if(hasPower(Pickup.Type.DOUBLE_DMG)){
            for(Projectile p : newFireballs){
                p.tint = Color.RED;
            }
        }
        if(hasPower(Pickup.Type.BOUNCING_BULLETS)){
            for(Projectile p : newFireballs){
                p.setBounces(2);
            }
        }
        adjustMana(-1.0f);
        screen.add(newFireballs);
        fireballs.addAll(newFireballs);
        changeState(attacking);
        castSound.play(0.3f);
    }

    void increaseDamage(int amount){
        this.attackDamage += amount;
    }

    public float getMaxSpeed(){
        return this.maxSpeed;
    }

    public void increaseMaxHP(int amount){
        maxHP += amount;
        health += amount;
    }

    public void increaseMaxSpeed(float amount){
        this.maxSpeed += amount;
        if( maxSpeed >= MAX_SPEED_LIMIT){
            maxSpeed = MAX_SPEED_LIMIT;
        }
    }

    public void pickup(Pickup p){
        if(p.TYPE != Pickup.Type.POTION && p.TYPE != Pickup.Type.BUFF){
            activePowers.add(p);
        }
        p.activate(this);

    }

    private void tickPower(float dt){
        for(Pickup power : activePowers){
            power.update(dt);
            if(((Pickup.TimedPickup) power).isTimeUp()){
                power.deactivate();
                activePowers.removeValue(power, false);
            }

        }
    }

    public boolean isDead(){
        return currentState == HeroState.dead;
    }

    public boolean hasPower(Pickup.Type type){
        for(Pickup power : activePowers){
            if (power.TYPE == type) return true;
        }
        return false;
    }

    public void adjustMana(float amount){
        if (hasPower(Pickup.Type.UNLIMITED_MANA)) return;

        this.mana += amount;
        if(mana < 0){
            mana = 0;
        }else if(mana > maxMana){
            mana = maxMana;
        }
        // update hud
        screen.getHud().updateMana(MathUtils.floor(this.mana));
    }

    public void addHealth(int amount){
        this.health += amount;
        // don't overheal
        if (health > maxHP) health = maxHP;
        screen.getHud().updateHealth(Math.round(this.health / this.maxHP));
    }

    /**
     * Damages the hero by the given amount.
     * @param damageAmount Amount of hero health to reduce
     */
    @Override
    public void damage(int damageAmount){
        if(!invincible && health > 0){
            System.out.printf("%d dmg taken! \n", damageAmount);
            this.health -= damageAmount;
            if (health <= 0){
                screen.getAssetManager().get("sounds/hero-death.wav", Sound.class).play();
                die();
            }else{
                invincible = true;
                screen.getAssetManager().get("sounds/hero-pain" + MathUtils.random(1,3) + ".ogg", Sound.class).play();
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
        shield.render(batch);
    }

    private void die(){
        if(footsteps.isPlaying()) footsteps.stop();
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

    public void dispose(){
        footsteps.stop();
        barrierSound.stop();
        screen.getWorld().destroyBody(shield.getBody());
    }
}
