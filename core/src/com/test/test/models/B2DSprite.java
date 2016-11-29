package com.test.test.models;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.test.test.utils.Animation;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by Fionn on 20/11/2016.
 */
public class B2DSprite{

    protected Body b2body;
    protected Sprite sprite;
    protected Animation animation;
    protected float width;
    protected float height;

    protected boolean setToDestroy;
    protected boolean destroyed;

    protected int health;

    public B2DSprite(){
        animation = new Animation();
        destroyed = false;
        setToDestroy = false;
        sprite = new Sprite();
    }

    public B2DSprite(Body body){
        this.b2body = body;
        animation = new Animation();
        destroyed = false;
        setToDestroy = false;
        sprite = new Sprite();
    }

    public void setTexture(TextureRegion texture){
        sprite.setBounds(0, 0, texture.getRegionWidth() / PPM, texture.getRegionHeight() / PPM);
        sprite.setRegion(texture);
        sprite.setOriginCenter();
    }

    public void setAnimation(TextureRegion[] reg, float delay) {
        animation.setFrames(reg, delay);
        width = reg[0].getRegionWidth();
        height = reg[0].getRegionHeight();
    }

    public void update(float dt) {
        sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                           b2body.getPosition().y - sprite.getHeight() / 2);
//        animation.update(dt);
    }

    public void render(SpriteBatch sb) {
        sprite.setRegion(animation.getFrame());
        sprite.draw(sb);
    }

    public void dispose(){
        this.b2body = null;
        this.sprite = null;
        this.animation = null;
    }

    public void setToDestroy(){ setToDestroy = true; }
    public boolean isSetToDestroy(){ return setToDestroy; }
    public boolean isDestroyed(){ return destroyed; }
    public Sprite getSprite(){ return this.sprite; }
    public Body getBody() { return b2body; }
    public Vector2 getPosition() { return b2body.getPosition(); }

    public float getWidth() { return width; }
    public float getHeight() { return height; }
}
