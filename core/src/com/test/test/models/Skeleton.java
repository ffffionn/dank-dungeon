package com.test.test.models;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;

/**
 * Skeleton Enemy type. Uses default Enemy attributes.
 */
public class Skeleton extends Enemy {

    private static TextureRegion[] moveAnimation;
    private static TextureRegion[] attackAnimation;

    public Skeleton(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.max_speed = 0.4f;
        this.health = 40;
        define(startPosition);

        if(moveAnimation == null){
            moveAnimation = screen.getAtlas().findRegion("rat-move").split(128, 128)[0];
        }
        if(attackAnimation == null){
            attackAnimation = screen.getAtlas().findRegion("rat-attack").split(128, 128)[0];
        }
        setTexture(moveAnimation[0], 16);
        setAnimation(moveAnimation, 1 / 12f);

    }

    @Override
    public void render(SpriteBatch batch){
        sprite.setRegion(animation.getFrame());
        // rotate region 90 first for perf.
        sprite.rotate90(true);
        sprite.draw(batch);
    }

}
