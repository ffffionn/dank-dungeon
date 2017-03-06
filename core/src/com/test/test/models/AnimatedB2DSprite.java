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
        this.animation = new Animation();
    }

    public AnimatedB2DSprite(TextureRegion[] frames){
        super();
        this.animation = new Animation(frames);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        animation.update(dt);
    }

    @Override
    public void render(SpriteBatch sb) {
        sprite.setRegion(animation.getFrame());
        sprite.draw(sb);
    }

    public void setAnimation(TextureRegion[] reg, float delay) {
        animation.setFrames(reg, delay);
    }
}
