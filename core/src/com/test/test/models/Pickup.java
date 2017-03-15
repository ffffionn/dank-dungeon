package com.test.test.models;

import com.badlogic.gdx.graphics.Texture;
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
 * Power-ups that bestow benefits upon the Hero when picked up.
 */
public class Pickup extends B2DSprite {

    public enum Type{ MULTI_FIRE, SPEEDUP, DOUBLE_DMG, POTION }
    public Type TYPE;

    protected int size;

    protected static TextureRegion healthBottle;
    protected static TextureRegion manaBottle;
    protected static TextureRegion chilli;
    protected static TextureRegion cheese;

    public Pickup(GameScreen screen, TextureRegion texture, Vector2 startPosition, int size){
        super();
        this.size = size;
        setTexture(texture, size);
        define(screen, startPosition);
    }

    public void update(float dt){ super.update(dt); }
    public void activate(Hero hero){}
    public void deactivate(){}

    protected void playSound(){

    }

    protected void updateHUD(){

    }

    public static void definePickupTextures(Texture itemTileSet){
        TextureRegion[][] items = TextureRegion.split(itemTileSet, 25, 25);
        healthBottle = items[2][1];
        manaBottle = items[2][2];
        chilli = items[5][4];
        cheese = items[5][3];
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
            TYPE = Type.POTION;
        }

        @Override
        public void update(float dt) {
            super.update(dt);
            timerCount += dt;
        }

        @Override
        public void activate(Hero hero) {
            timerCount = 0;
        }

        public boolean isTimeUp(){ return timerCount >= powerTimer; }
    }


    // CONCRETE PICKUP OBJECTS BELOW

    /** GIVE PLAYER +15 HEALTH */
    public static class HealthPickup extends Pickup {
        public HealthPickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, healthBottle, startPosition, size);
            TYPE = Type.POTION;
        }

        @Override
        public void activate(Hero hero){
            hero.addHealth(15);
        }
    }

    /** GIVE PLAYER +10 MANA */
    public static class ManaPickup extends Pickup {
        public ManaPickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, manaBottle, startPosition, size);
            TYPE = Type.POTION;
        }

        @Override
        public void activate(Hero hero){
            hero.adjustMana(20.0f);
        }
    }

    /** GIVE PLAYER MORE FIREBALLS */
    public static class MultifirePickup extends TimedPickup{
        public MultifirePickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, chilli, startPosition, size);
            TYPE = Type.MULTI_FIRE;
            powerTimer = 5.0f;
            timerCount = 0;
        }

        @Override
        public void activate(Hero hero){
            timerCount = 0.0f;
        }
    }

    /** GIVE FIREBALLS DOUBLE DAMAGE */
    public static class DoubleDamagePickup extends TimedPickup {
        public DoubleDamagePickup(GameScreen screen, Vector2 startPosition, int size){
            super(screen, cheese, startPosition, size);
            TYPE = Type.DOUBLE_DMG;
            powerTimer = 5.0f;
            timerCount = 0;
        }

        @Override
        public void activate(Hero hero){
            timerCount = 0.0f;
        }
    }

}
