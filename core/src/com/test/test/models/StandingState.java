package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Created by Fionn on 04/02/2017.
 */
public class StandingState extends HeroState {

    private boolean still;
    private float timeStanding;

    @Override
    public void update(float dt, Hero hero) {
        timeStanding += dt;

        if(Gdx.input.isKeyPressed(Input.Keys.SPACE) && hero.getMana() > 10.0f){
            hero.block();
        }
        if(Gdx.input.justTouched()){
            hero.shoot();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && sprinting.canRun()){
            hero.changeState(sprinting);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            hero.damage(10);
        }
        if( Math.abs(hero.getBody().getLinearVelocity().x) < 0.1f &&
                Math.abs(hero.getBody().getLinearVelocity().y) < 0.1f ){
            if(!still){
                still = true;
                hero.setAnimation(standAnimation, 0f);
            }
        }else if(still){
            hero.setAnimation(standAnimation, 1/8f);
            still = false;
        }
        handleMovement(hero);
    }

    @Override
    public void enter(Hero hero){
        timeStanding = 0.0f;
        still = false;
        hero.setAnimation(standAnimation, 1/8f);
    }

    public float getTimeStanding(){
        return timeStanding;
    }

    public String toString(){return "standing";}
}
