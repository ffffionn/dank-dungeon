package com.test.test.models;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

import static com.test.test.DankDungeon.PPM;

/**
 * World entity that has a physics body and a graphic sprite.
 */
public class B2DSprite{

    protected Body b2body;
    protected Sprite sprite;

    protected boolean setToDestroy;
    protected boolean destroyed;

    protected int health;

    public B2DSprite(){
        sprite = new Sprite();
        destroyed = false;
        setToDestroy = false;
    }

    public B2DSprite(Body body, Sprite sprite){
        this.b2body = body;
        this.sprite = sprite;
        destroyed = false;
        setToDestroy = false;
        sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                b2body.getPosition().y - sprite.getHeight() / 2);
    }

    public void setTexture(TextureRegion texture, int size){
        setTexture(texture, size, size);
    }

    public void setTexture(TextureRegion texture, int width, int height){
        sprite.setBounds(0, 0, width / PPM, height / PPM);
        sprite.setRegion(texture);
        sprite.setOriginCenter();
    }

    public void update(float dt) {
        if(sprite != null){
            sprite.setPosition(b2body.getPosition().x - sprite.getWidth() / 2,
                           b2body.getPosition().y - sprite.getHeight() / 2);
            sprite.setRotation(b2body.getAngle() * MathUtils.radiansToDegrees);
        }
    }

    public void render(SpriteBatch sb) {
        sprite.draw(sb);
    }

    public void dispose(){
        this.b2body = null;
        this.sprite = null;
    }

    public void damage(int dmgAmount){
        this.health -= dmgAmount;
        if( health < 0){
            setToDestroy();
        }
    }

    public Body getBody() { return b2body; }
    public Sprite getSprite(){ return this.sprite; }
    public void setToDestroy(){ setToDestroy = true; }
    public boolean isDestroyed(){ return destroyed; }
    public boolean isSetToDestroy(){ return setToDestroy; }
    public Vector2 getPosition() { return b2body.getPosition(); }
}
