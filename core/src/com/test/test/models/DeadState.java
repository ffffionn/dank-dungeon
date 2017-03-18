package com.test.test.models;

/**
 * State to play a Death Animation.
 */
public class DeadState extends HeroState {

    @Override
    public void enter(Hero hero) {
        hero.setAnimation(dieAnimation, 1/6f);
    }

    @Override
    public void update(float dt, Hero hero) {
        // don't handle input, you're dead!
    }

    public String toString(){
        return "DEAD";
    }
}
