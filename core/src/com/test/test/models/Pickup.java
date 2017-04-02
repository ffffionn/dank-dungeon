package com.test.test.models;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.utils.WorldContactListener.*;

/**
 * Power-ups that bestow benefits upon the Hero when picked up.
 */
public class Pickup extends B2DSprite {

    public enum Type{ MULTI_FIRE, INVINCIBLE, BOUNCING_BULLETS, DOUBLE_DMG, POTION, UNLIMITED_MANA, BUFF}
    public Type TYPE;

    protected int size;
    protected GameScreen screen;

    protected static TextureRegion healthBottle;
    protected static TextureRegion manaBottle;
    protected static TextureRegion chili;
    protected static TextureRegion ham;
    protected static TextureRegion cheese;
    protected static TextureRegion grapes;
    protected static TextureRegion mushrooms;
    protected static TextureRegion staff;
    protected static TextureRegion boots;
    protected static TextureRegion shield;

    public Pickup(GameScreen screen, TextureRegion texture, Vector2 startPosition, int size){
        super();
        this.TYPE = Type.POTION;
        this.screen = screen;
        this.size = size;
        setTexture(texture, size);
        define(screen, startPosition);
    }

    public void update(float dt){ super.update(dt); }
    public void activate(Hero hero){}
    public void deactivate(){}

    protected void playSound(String soundID, float volume){
        screen.getAssetManager().get("sounds/" + soundID, Sound.class).play(volume);
    }

    protected void updateHUD(){

    }

    public static void definePickupTextures(TextureRegion itemTileSet){
        TextureRegion[][] items = itemTileSet.split(25, 25);
        healthBottle = items[2][1];
        manaBottle = items[2][2];
        chili = items[5][4];
        cheese = items[5][3];
        grapes = items[5][1];
        mushrooms = items[5][7];
        ham = items[6][0];
        staff = items[1][8];
        boots = items[4][0];
        shield = items[1][0];
    }

    protected void define(GameScreen screen, Vector2 position){
        BodyDef bdef = new BodyDef();
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = true;
        bdef.linearDamping = 0.0f;
        this.b2body = screen.getWorld().createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(this.size / MathUtils.PI / PPM);
        fdef.shape = shape;
        fdef.friction = 0.0f;
        fdef.restitution = 0.0f;
        fdef.isSensor = true;

        fdef.filter.categoryBits = PICKUP;
        fdef.filter.maskBits = PLAYER;
        b2body.createFixture(fdef).setUserData("pickup");
        b2body.setUserData(this);
    }


    public static class TimedPickup extends Pickup {
        protected float powerTimer;
        protected float timerCount;

        public TimedPickup(GameScreen screen, TextureRegion texture, Vector2 startPosition, int size) {
            super(screen, texture, startPosition, size);
        }

        @Override
        public void update(float dt) {
            super.update(dt);
            timerCount += dt;
        }

        @Override
        public void activate(Hero hero){
            timerCount = 0.0f;
            playSound("hero-munch.ogg", 0.9f);
        }

        public boolean isTimeUp(){ return timerCount >= powerTimer; }
    }


    public static class BuffPickup extends Pickup {

        public BuffPickup(GameScreen screen, TextureRegion texture, Vector2 startPosition, int size) {
            super(screen, texture, startPosition, size);
            TYPE = Type.BUFF;
        }

        @Override
        public void activate(Hero hero){
            playSound("hero-powerup.wav", 0.7f);
            screen.getHud().flash(Color.GOLDENROD, Interpolation.exp10Out, 0.8f);
        }

    }


    // CONCRETE PICKUP OBJECTS BELOW

    /** GIVE PLAYER +15 HEALTH */
    public static class HealthPotion extends Pickup {
        public HealthPotion(GameScreen screen, Vector2 startPosition, int size){
            super(screen, healthBottle, startPosition, size);
            TYPE = Type.POTION;
        }

        @Override
        public void activate(Hero hero){
            hero.addHealth(15);
            playSound("hero-heal.ogg", 0.6f);
        }
    }

    /** GIVE PLAYER +10 MANA */
    public static class ManaPotion extends Pickup {
        public ManaPotion(GameScreen screen, Vector2 startPosition, int size){
            super(screen, manaBottle, startPosition, size);
            TYPE = Type.POTION;
        }

        @Override
        public void activate(Hero hero){
            hero.adjustMana(20.0f);
            playSound("hero-healed.ogg", 0.5f);
        }
    }

    /** GIVE PLAYER A PERMANENT DAMAGE BOOST */
    public static class StaffPickup extends BuffPickup {
        public StaffPickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, staff, startPosition, size);
        }

        @Override
        public void activate(Hero hero){
            super.activate(hero);
            hero.increaseDamage(5);
        }
    }


    /** GIVE PLAYER PERMANENT MORE MAX HP */
    public static class HPPickup extends BuffPickup {
        public HPPickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, shield, startPosition, size);
        }

        @Override
        public void activate(Hero hero){
            super.activate(hero);
            hero.increaseMaxHP(15);

        }
    }


    /** GIVE PLAYER A PERMANENT DAMAGE BOOST */
    public static class BootsPickup extends BuffPickup {
        public BootsPickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, boots, startPosition, size);
        }

        @Override
        public void activate(Hero hero){
            super.activate(hero);
            hero.increaseMaxSpeed(0.15f);
        }
    }



    /** GIVE PLAYER MORE FIREBALLS */
    public static class MultifirePickup extends TimedPickup{
        public MultifirePickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, chili, startPosition, size);
            TYPE = Type.MULTI_FIRE;
            powerTimer = 10.0f;
            timerCount = 0;
        }
    }

    /** GIVE FIREBALLS DOUBLE DAMAGE */
    public static class DoubleDamagePickup extends TimedPickup {
        public DoubleDamagePickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, cheese, startPosition, size);
            TYPE = Type.DOUBLE_DMG;
            powerTimer = 10.0f;
            timerCount = 0;
        }
    }

    /** GIVE PLAYER UNLIMITED MANA */
    public static class UnlimitedManaPickup extends TimedPickup {
        public UnlimitedManaPickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, grapes, startPosition, size);
            TYPE = Type.UNLIMITED_MANA;
            powerTimer = 7.5f;
            timerCount = 0;
        }
    }

    /** MAKE THE PLAYER INVINCIBLE AND DEAL DMG ON HIT */
    public static class InvinciblePickup extends TimedPickup {
        public InvinciblePickup(GameScreen screen, Vector2 startPosition, int size) {
            super(screen, mushrooms, startPosition, size);
            TYPE = Type.INVINCIBLE;
            powerTimer = 5.0f;
            timerCount = 0;
        }

        @Override
        public void activate(Hero hero) {
            super.activate(hero);
            screen.getHud().flash(Color.GOLD, Interpolation.pow2, powerTimer);
//            screen.getHud().tintFor(Color.GOLD, powerTimer);
        }

        @Override
        public void deactivate() {
            System.out.println("power gone");
        }
    }

    /** GIVE THE PLAYER BOUNCING PROJECTILES */
    public static class BouncingProjectilePickup extends TimedPickup {
        public BouncingProjectilePickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, ham, startPosition, size);
            TYPE = Type.BOUNCING_BULLETS;
            powerTimer = 7.5f;
            timerCount = 0;
        }
    }

}
