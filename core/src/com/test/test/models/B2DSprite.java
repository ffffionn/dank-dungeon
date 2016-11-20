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

    public B2DSprite(Body body){
        this.b2body = body;
        animation = new Animation();
    }

    public void setAnimation(TextureRegion[] reg, float delay) {
        animation.setFrames(reg, delay);
        width = reg[0].getRegionWidth();
        height = reg[0].getRegionHeight();
    }

    public void update(float dt) {
        animation.update(dt);
    }

    public void render(SpriteBatch sb) {
        sb.begin();
        sb.draw(
                animation.getFrame(),
                b2body.getPosition().x * PPM - width / 2,
                b2body.getPosition().y * PPM - height / 2
        );
        sb.end();
    }

    public Body getBody() { return b2body; }
    public Vector2 getPosition() { return b2body.getPosition(); }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

}
