package com.test.test.models;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;


/**
 * Wolf Enemy type. Slow and tanky enemy, deals high damage.
 */
public class Wolf extends Enemy {

    private static TextureRegion[] moveAnimation;
    private static TextureRegion[] attackAnimation;
    private static TextureRegion[] deathAnimation;

    private static float CHARGE_SPEED = 1.35f;

    private float chargeCooldown;

    private boolean charging;

    public Wolf(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);

        // Wolf attributes
        this.health = this.maxHealth = 300;
        this.max_speed = 0.45f;
        this.score_value = 200;
        this.attackDamage = 9;
        this.radius = 9.0f;
        this.maxSight = 1.8f;
        this.coneAngle = 30 * MathUtils.degreesToRadians;
        this.canAttack = true;

        this.chargeCooldown = 2.5f;
        this.charging = false;
        // first enemy fetches animation frames from TextureAtlas
        if(moveAnimation == null || attackAnimation == null){
            defineAnimations(screen);
        }
        setTexture(moveAnimation[0], 24);
        setAnimation(moveAnimation, 1 / 12f);

        define(startPosition);
    }

    public Wolf(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    public Wolf(GameScreen screen, Vector2 startPosition, float seed){
        this(screen, startPosition);
        this.level = 1 + MathUtils.floor(seed * 50);

        health = maxHealth = 200 + level * 20;
        score_value = 50 * level;
        attackDamage = 10 + level / 2;
    }



    @Override
    protected void move() {
        float distanceToTarget = b2body.getPosition().dst(target.cpy());
        if (charging) {
            moveTowards(target.cpy());
            avoidWalls();
            if (Math.abs(b2body.getLinearVelocity().x) < 0.1f &&
                    Math.abs(b2body.getLinearVelocity().y) < 0.1f) {
                stopCharge();
            } else if (distanceToTarget < 0.01f && distanceToTarget > 0){
                stopCharge();
            }
        } else {
            if (targetInSight()) {
                moveTowards(target.cpy());
                if (distanceToTarget <= 1.0f && canAttack) {
                    // attack when in range
                    attack();
                }
            } else {
                this.max_speed = 0.45f;
                walkAround();
            }
        }
    }


    private void stopCharge(){
        setAnimation(moveAnimation, 1 / 12f);
        charging = false;
        canAttack = false;
        max_speed = 0.45f;
        attackDamage = 10 + level / 2;

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                canAttack = true;
            }
        }, chargeCooldown);
    }

    private void attack(){
        charging = true;
        this.attackDamage = Math.round(attackDamage * 1.2f);

        // play attack animation for duration
        this.max_speed = CHARGE_SPEED;
        setAnimation(moveAnimation, 1 / 30f);

        // charge for at most 1.2s
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if(!destroyed && !setToDestroy && charging){
                    stopCharge();
                }
            }
        }, 1.2f);

    }

    @Override
    public void setTarget(Vector2 target) {
        if(!charging){
            super.setTarget(target);
        }
    }

    @Override
    protected void setDeathAnimation() {
        setAnimation(deathAnimation, 1 / 12f);
    }

    @Override
    protected void playDeathSound() {
        screen.getAssetManager().get("sounds/wolf-death.wav", Sound.class).play();
    }

    @Override
    protected void playHurtSound() {
        screen.getAssetManager().get("sounds/wolf-pain.wav", Sound.class).play();
    }

    public static void defineAnimations(GameScreen screen){
        TextureRegion[][] frames = screen.getAtlas().findRegion("wolfbeast-move").split(64, 64);
        moveAnimation = new TextureRegion[16];
        int index = 0;
        for(int x = 0; x < frames.length; x++){
            for(int y = 0; y < frames[x].length; y++){
                moveAnimation[index++] = frames[x][y];
            }
        }

        frames = screen.getAtlas().findRegion("wolfbeast-attack").split(64, 64);
        attackAnimation = new TextureRegion[16];
        index = 0;
        for(int x = 0; x < frames.length; x++){
            for(int y = 0; y < frames[x].length; y++){
                attackAnimation[index++] = frames[x][y];
            }
        }

        frames = screen.getAtlas().findRegion("wolfbeast-death").split(64, 64);
        deathAnimation = new TextureRegion[32];
        index = 0;
        for(int x = 0; x < frames.length; x++){
            for(int y = 0; y < frames[x].length; y++){
                deathAnimation[index++] = frames[x][y];
            }
        }

    }

}
