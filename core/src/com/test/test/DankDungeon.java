package com.test.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.test.test.screens.LoadingScreen;

public class DankDungeon extends Game {

    public static int V_WIDTH = 1024;
    public static int V_HEIGHT = 760;
    public static final float PPM = 80;

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
        log.log();
	}

	@Override
	public void dispose () {
        super.dispose();
		batch.dispose();
	}

}
