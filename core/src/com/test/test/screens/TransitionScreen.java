package com.test.test.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;
import com.test.test.DankDungeon;

/**
 * Created by Fionn on 20/03/2017.
 */
public class TransitionScreen implements Screen {

    DankDungeon game;

    Screen current;
    Screen next;

    int currentTransitionEffect;
    Array<TransitionEffect> transitionEffects;

    public TransitionScreen(DankDungeon game, Screen current, Screen next, Array<TransitionEffect> transitionEffects) {
        this.current = current;
        this.next = next;
        this.transitionEffects = transitionEffects;
        this.currentTransitionEffect = 0;
        this.game = game;
    }


    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (currentTransitionEffect >= transitionEffects.size) {
            game.setScreen(next);
            return;
        }

        transitionEffects.get(currentTransitionEffect).update(delta);


        if (transitionEffects.get(currentTransitionEffect).isFinished())
            currentTransitionEffect++;

        game.batch.begin();
        transitionEffects.get(currentTransitionEffect).render(delta, current, next);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
