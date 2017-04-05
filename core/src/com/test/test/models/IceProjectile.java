package com.test.test.models;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;

/**
 * An extension of the Projectile class that stun enemies.
 */
public class IceProjectile extends Projectile {

    protected float radius = 1.5f / PPM;
    protected static TextureRegion[] icicleAnimation;

    public IceProjectile(GameScreen screen, Vector2 startPosition, Vector2 target){
        super(screen, startPosition, target);

        if(icicleAnimation == null){
            icicleAnimation = screen.getAtlas().findRegion("icicle").split(64, 64)[4];
        }
        setTexture(icicleAnimation[0], 12);
        setAnimation(icicleAnimation, 1 / 12f);
    }


    public IceProjectile(GameScreen screen, Vector2 startPosition, Vector2 target, int damage){
        this(screen, startPosition, target);
        this.damageAmount = damage;
    }


}
