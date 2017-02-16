package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Created by Fionn on 15/02/2017.
 */
public class AttackingState extends HeroState {

    private float timer;

    @Override
    public void update(float dt, Hero hero) {
        // if space released or mana empty, return to previous state
        handleMovement(hero);
        timer += dt;
        if(timer > 0.15f){
            hero.changeState(hero.getPreviousState());
        }
    }

    @Override
    public void enter(Hero hero){
        timer = 0.0f;
        hero.setAnimation(castAnimation, 1 / 32f);
    }

    public String toString(){ return "attacking"; }


}
