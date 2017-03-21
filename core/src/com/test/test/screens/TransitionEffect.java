package com.test.test.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;

/**
 * Created by Fionn on 20/03/2017.
 */
public class TransitionEffect {

    protected float duration;
    protected float timeElapsed;

    public TransitionEffect(float duration){
        this.duration = duration;
        timeElapsed = 0;
    }

    public void update(float delta){
        timeElapsed += delta;
    }

    public void render(float delta, Screen current, Screen next){

    }

    public boolean isFinished(){
        return timeElapsed > duration;
    }

    protected float getAlpha(){
        return timeElapsed / duration;
    }


    public static class FadeOut extends TransitionEffect {

        Color color = new Color();


        public FadeOut(float duration) {
            super(duration);
        }

        @Override
        public void render(float delta, Screen current, Screen next) {
            current.render(delta);
            color.set(0f, 0f, 0f, getAlpha());
            // draw a quad over the screen using the color
        }

    }

    public static class FadeIn extends TransitionEffect {

        Color color = new Color();

        public FadeIn(float duration){
            super(duration);
        }

        @Override
        public void render(float delta, Screen current, Screen next) {
            current.render(delta);
            color.set(0f, 0f, 0f, getAlpha());
            // draw a quad over the screen using the color
        }

        @Override
        protected float getAlpha() {
            return 1 / ( timeElapsed / duration );
        }
    }
}
