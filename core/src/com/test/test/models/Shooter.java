package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.test.test.screens.GameScreen;

/**
 * Shooter Enemy type. Fires projectiles at the hero, runs away if approached.
 */
public class Shooter extends Enemy{

    public Shooter(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.sightRadius = 3.5f;
        this.max_speed =  0.85f;
        this.radius = 4.20f;
        this.score_value = 150;
        this.health = 60;

        define(startPosition);
    }

    public Shooter(GameScreen screen, Vector2 startPosition, float speed, int hp){
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
               shoot(target);
           }
       }else{
//           moveRandom();
       }
    }


    private void shoot(Vector2 target){
        if(canAttack){
            this.canAttack = false;
            faceDirection(MathUtils.atan2((target.y - b2body.getPosition().y), (target.x - b2body.getPosition().x)));
            Projectile p = new Projectile(screen, getPosition(), target, 10, 1.25f);
            screen.add(p);
            // can't shoot again for 1s
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    canAttack = true;
                }
            }, 1f);
        }
    }

    private void moveRandom(){
        float angle = b2body.getAngle();
        int direction = Math.round((angle + MathUtils.PI2) / (MathUtils.PI));
        System.out.printf("%f -> %d \n", angle + MathUtils.PI2, direction);
    }
}
