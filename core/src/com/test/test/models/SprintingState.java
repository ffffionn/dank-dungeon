package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

/**
 * Created by Fionn on 04/02/2017.
 */
public class SprintingState extends HeroState {

    private static float RUN_MODIFIER = 1.6f;
    private static final float SPRINT_COOLDOWN = 1.75f;
    private boolean canRun;
    private float timeRunning;
    private Vector2 heroVelocity;

    public SprintingState(){
        this.canRun = true;
    }

    @Override
    public void update(float dt, Hero hero) {
        handleMovement(hero);
        timeRunning += dt;
        heroVelocity = hero.getBody().getLinearVelocity();

        // if you're not running, or you can't, switch to standing
        if(timeRunning > 1.5f || !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                (Math.abs(heroVelocity.x) < 0.01f && Math.abs(heroVelocity.y) < 0.01f) ){
            hero.changeState(standing);
        }
    }

    @Override
    public void enter(Hero hero) {
        timeRunning = 0.0f;
        hero.setAnimation(runAnimation, 1/8f);
    }

    @Override
    public void leave(Hero hero){
        canRun = false;
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                canRun = true;
            }
        }, SPRINT_COOLDOWN);
    }

    @Override
    protected float getRunModifier(){return RUN_MODIFIER;}
    public String toString(){ return "running";}
    public float getTimeRunning(){return timeRunning;}
    public boolean canRun(){ return this.canRun; }
}
