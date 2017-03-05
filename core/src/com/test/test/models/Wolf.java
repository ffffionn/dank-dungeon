package com.test.test.models;

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

    public Wolf(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);

        // Wolf attributes
        this.health = this.maxHealth = 300;
        this.max_speed = 0.35f;
        this.score_value = 200;
        this.attackDamage = 9;
        this.radius = 9.0f;
        this.maxSight = 1.8f;
        this.coneAngle = 60 * MathUtils.degreesToRadians;
        this.canAttack = true;
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

    @Override
    protected void move() {
        if(targetInSight()){
            moveTowards(target);
            System.out.println(b2body.getPosition().dst(target));
            if( b2body.getPosition().dst(target) <= 0.5f && canAttack ) {
                // attack when in range
                attack();
            }
        }else{
            walkAround();
            faceDirection(b2body.getLinearVelocity().angleRad());
        }
    }

    private void attack(){
        // do stuff
        // play attack animation for duration
        System.out.println("!");
        setAnimation(attackAnimation, 1 / 24f);
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if(!destroyed && !setToDestroy) setAnimation(moveAnimation, 1 / 12f);
            }
        }, 0.8f);
        // can't shoot again for 1s
        this.canAttack = false;
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                canAttack = true;
            }
        }, 1f);
    }


    @Override
    protected void setDeathAnimation() {
        setAnimation(deathAnimation, 1 / 12f);
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
