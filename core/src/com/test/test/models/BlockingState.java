package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Created by Fionn on 04/02/2017.
 */
public class BlockingState extends HeroState {

    @Override
    public void enter(Hero hero) {
        hero.setAnimation(blockAnimation, 1 / 6f);
    }

    @Override
    public void handleInput(Hero hero) {
        super.handleInput(hero);
        if (!Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            hero.unblock();
        }
    }

    @Override
    public void update(float dt) {

    }

    public String toString(){
        return "blocking";
    }
}
