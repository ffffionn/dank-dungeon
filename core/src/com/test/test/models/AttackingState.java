package com.test.test.models;


/**
 * Created by Fionn on 15/02/2017.
 */
public class AttackingState extends HeroState {

    private float timer;

    @Override
    public void update(float dt, Hero hero) {
        // stay in this state until the animation has played a sufficient amount of time
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
