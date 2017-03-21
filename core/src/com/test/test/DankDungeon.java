package com.test.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.test.test.screens.LoadingScreen;
import com.test.test.screens.TransitionEffect;
import com.test.test.screens.TransitionScreen;

public class DankDungeon extends Game {

    public static int V_WIDTH = 1024;
    public static int V_HEIGHT = 760;
    public static final float PPM = 100;

    public SpriteBatch batch;

    private FPSLogger log;

	@Override
	public void create () {
        this.batch = new SpriteBatch();
        this.log = new FPSLogger();

        setScreen(new LoadingScreen(this));
	}

	@Override
	public void render () {
        super.render();
//        log.log();
	}

	@Override
	public void dispose () {
        super.dispose();
		batch.dispose();
	}

	public void switchScreen(Screen screen){
        Array<TransitionEffect> effects = new Array<TransitionEffect>();

        effects.add(new TransitionEffect.FadeOut(1f));
        effects.add(new TransitionEffect.FadeIn(1f));
        setScreen(new TransitionScreen(this, getScreen(), screen, effects));
    }

}
