package com.test.test.models;

/**
 * Created by Fionn on 04/02/2017.
 */
public class DeadState extends HeroState {

    @Override
    public void enter(Hero hero) {
        hero.setAnimation(dieAnimation, 1/6f);
    }

    @Override
    public void handleInput(Hero hero) {
        // don't handle input, you're dead!
    }

    public String toString(){
        return "DEAD";
    }
}
