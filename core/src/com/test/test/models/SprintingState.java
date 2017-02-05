package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Fionn on 04/02/2017.
 */
public class SprintingState extends HeroState {

    protected static float RUN_MODIFIER = 2.0f;

    @Override
    public void handleInput(Hero hero) {
        Vector2 heroVelocity = hero.getBody().getLinearVelocity();

        // if shift is not held or you're not moving, switch to standing
        if(!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
                (Math.abs(heroVelocity.x) < 0.1f && Math.abs(heroVelocity.y) < 0.1f) ){
            hero.changeState(standing);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.W) && heroVelocity.y < Hero.MAX_VELOCITY) {
            hero.getBody().applyLinearImpulse(new Vector2(0, 0.2f * RUN_MODIFIER), hero.getBody().getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && heroVelocity.y > -Hero.MAX_VELOCITY) {
            hero.getBody().applyLinearImpulse(new Vector2(0, -0.2f * RUN_MODIFIER), hero.getBody().getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && heroVelocity.x < Hero.MAX_VELOCITY) {
            hero.getBody().applyLinearImpulse(new Vector2(0.2f * RUN_MODIFIER, 0),
                    hero.getBody().getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && heroVelocity.x > -Hero.MAX_VELOCITY) {
            hero.getBody().applyLinearImpulse(new Vector2(-0.2f * RUN_MODIFIER, 0), hero.getBody().getWorldCenter(), true);
        }
    }

    @Override
    public void enter(Hero hero) {
        hero.setAnimation(runAnimation, 1/8f);
    }

    public String toString(){
        return "running";
    }

}
