package com.test.test.models;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;

/**
 * Rat Enemy type. Uses default Enemy attributes.
 */
public class Rat extends Enemy {

    private static TextureRegion[] moveAnimation;
    private static TextureRegion[] attackAnimation;
    private static TextureRegion[] deathAnimation;

    public Rat(GameScreen screen, Vector2 startPosition) {
        super(screen, startPosition);
        this.max_speed = 0.4f;
        this.health = this.maxHealth = 40;
        this.radius = 3.0f;
        define(startPosition);

        if (moveAnimation == null || attackAnimation == null || deathAnimation == null) {
            defineAnimations(screen);
        }
        setTexture(moveAnimation[0], 16);
        setAnimation(moveAnimation, 1 / 12f);

    }


    public Rat(GameScreen screen, Vector2 startPosition, float seed){
        this(screen, startPosition);
        this.level = 1 + MathUtils.floor(seed * 50);

        health = maxHealth = 20 + (level / 10) * 20;
        score_value = 5 * level;
        attackDamage = 5 + level / 3;
    }

    @Override
    protected void setDeathAnimation() {
        setAnimation(deathAnimation, 1 / 8f);
    }


    @Override
    protected void playDeathSound() {
        screen.getAssetManager().get("sounds/rat-death.ogg", Sound.class).play(0.6f);
    }

    @Override
    protected void playHurtSound() {
        screen.getAssetManager().get("sounds/rat-pain.ogg", Sound.class).play(0.6f);
    }


    public static void defineAnimations(GameScreen screen) {
        moveAnimation = screen.getAtlas().findRegion("rat-move").split(128, 128)[0];
        attackAnimation = screen.getAtlas().findRegion("rat-attack").split(128, 128)[0];
        deathAnimation = new TextureRegion[8];
        TextureRegion[][] frames = screen.getAtlas().findRegion("rat-death").split(128, 128);
        int index = 0;
        for (int x = 0; x < frames.length; x++) {
            for (int y = 0; y < frames[x].length; y++) {
                deathAnimation[index++] = frames[x][y];
            }
        }
    }
}