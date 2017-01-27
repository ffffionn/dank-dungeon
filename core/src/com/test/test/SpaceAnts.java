package com.test.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.test.test.screens.GameScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SpaceAnts extends Game {

    public static int V_WIDTH = 800;
    public static int V_HEIGHT = 480;

    public static final float PPM = 100;

    public static OrthographicCamera cam;

    public SpriteBatch batch;

    private Runnable sleepTask, countTask;

	private static final int NUM_THREADS = 4;
	private static final int MAX_TASKS = 4;

    private ExecutorService pool = Executors.newFixedThreadPool(NUM_THREADS);
	private List<GameWorker> workers;
	private ConcurrentLinkedQueue<Runnable> tasks;

    private FPSLogger log;

	@Override
	public void create () {
        this.batch = new SpriteBatch();
        this.log = new FPSLogger();
    	this.workers = new ArrayList<GameWorker>();
        this.tasks = new ConcurrentLinkedQueue<Runnable>();

        setScreen(new GameScreen(this));
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
        try {
            System.out.println("attempt to shutdown executor");
            pool.shutdown();
            pool.awaitTermination(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            System.err.println("tasks interrupted");
        }
        finally {
            if (!pool.isTerminated()) {
                System.err.println("cancel non-finished tasks");
            }
            pool.shutdownNow();
            System.out.println("shutdown finished");
        }
	}

}
