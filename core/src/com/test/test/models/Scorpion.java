package com.test.test.models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

/**
 * Scorpion Enemy type. Wide field-of-view, shoots at the hero, runs away if approached.
 */
public class Scorpion extends Enemy{

    private static TextureRegion[] moveAnimation;
    private static TextureRegion[] attackAnimation;
    private static TextureRegion[] deathAnimation;

    public Scorpion(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.radius = 4.50f;
        this.maxSight = 3.0f;
        this.coneAngle = 120 * MathUtils.degreesToRadians;
        this.max_speed =  0.55f;
        this.score_value = 150;
        this.health = this.maxHealth = 70;
        define(startPosition);

        if(moveAnimation == null){
            moveAnimation = screen.getAtlas().findRegion("scorpion-move").split(64, 64)[0];
        }
        if(attackAnimation == null){
            attackAnimation = screen.getAtlas().findRegion("scorpion-attack").split(64, 64)[0];
        }
        if(deathAnimation == null){
            deathAnimation = new TextureRegion[16];
            int index = 0;
            for(TextureRegion t : screen.getAtlas().findRegion("scorpion-death-0").split(64, 64)[0]){
                deathAnimation[index++] = t;
            }
            for(TextureRegion t : screen.getAtlas().findRegion("scorpion-death-1").split(64, 64)[0]){
                deathAnimation[index++] = t;
            }
        }
        setTexture(moveAnimation[0], 18);
        setAnimation(moveAnimation, 1 / 12f);
    }

    public Scorpion(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    @Override
    protected void move() {
       if(targetInSight()){
           if( b2body.getPosition().dst(target) < 0.3f ){
               // flee opposite direction if player too close
               Vector2 approach = b2body.getPosition().cpy().sub(target);
               moveTowards(approach.add(b2body.getPosition()));
           }else{
               if(canAttack){
                   shoot(target);
               }else{
                   walkAround();
               }
               faceDirection(angleToTarget());
           }
       }else{
           walkAround();
           faceDirection(b2body.getLinearVelocity().angleRad());
       }
    }

    private void shoot(Vector2 target){
        if(canAttack){
            this.canAttack = false;
            screen.add(new Projectile(screen, getPosition(), target, 10, 1.25f));
            // play attack animation for duration
            setAnimation(attackAnimation, 1 / 18f);
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if(!destroyed && !setToDestroy) setAnimation(moveAnimation, 1 / 12f);
                }
            }, 0.3f);
            // can't shoot again for 1s
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    canAttack = true;
                }
            }, 1f);
        }
    }

    @Override
    public void damage(int dmgAmount){
        this.health -= dmgAmount;
        if( health <= 0){
            setToDestroy();
        }
    }
}
