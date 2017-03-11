package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Created by Fionn on 04/02/2017.
 */
public class BlockingState extends HeroState {


    @Override
    public void update(float dt, Hero hero) {
        // if right mouse is released or mana empty, return to previous state
        if (!Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || hero.getMana() < 2.5f){
            hero.unblock();
        }
        handleMovement(hero);

    }

    @Override
    public void enter(Hero hero){ hero.setAnimation(blockAnimation, 1 / 6f);}
    public String toString(){ return "blocking"; }
}
