package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * The default state for the Hero, handles input.
 */
public class StandingState extends HeroState {

    private boolean still;
    private float timeStanding;

    @Override
    public void update(float dt, Hero hero) {
        timeStanding += dt;

        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            hero.shoot();
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && hero.getMana() > 5.0f){
            hero.block();
        }
        if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && sprinting.canRun()){
            hero.changeState(sprinting);
        }
        if( Math.abs(hero.getBody().getLinearVelocity().x) < 0.1f &&
                Math.abs(hero.getBody().getLinearVelocity().y) < 0.1f ){
            if(!still){
                still = true;
                hero.setAnimation(standAnimation, 0f);
                hero.footsteps.stop();
            }
        }else if(still){
            hero.setAnimation(standAnimation, 1 / 8f);
            hero.footsteps.play();
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
