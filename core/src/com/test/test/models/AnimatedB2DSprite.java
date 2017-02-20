package com.test.test.models;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.test.test.utils.Animation;

/**
 * In-game entities that have animations.
 */
public class AnimatedB2DSprite extends B2DSprite {

    protected Animation animation;

    public AnimatedB2DSprite(){
        super();
        animation = new Animation();
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        animation.update(dt);
    }

    public void setAnimation(TextureRegion[] reg, float delay) {
        animation.setFrames(reg, delay);
    }

    @Override
    public void render(SpriteBatch sb) {
        sprite.setRegion(animation.getFrame());
        sprite.draw(sb);
    }
}
