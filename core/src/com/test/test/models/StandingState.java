package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Created by Fionn on 04/02/2017.
 */
public class StandingState extends HeroState {

    @Override
    public void enter(Hero hero) {
        hero.setAnimation(standAnimation, 1/4f);
    }

    @Override
    public void handleInput(Hero hero) {
        super.handleInput(hero);
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)){
            hero.changeState(sprinting);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
            hero.damage(10);
        }
        if( Math.abs(hero.getBody().getLinearVelocity().x) < 0.1f &&
                Math.abs(hero.getBody().getLinearVelocity().y) < 0.1f ){
//            System.out.print(".");
        }
    }

    public String toString(){
        return "standing";
    }
}
