package com.test.test;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.test.test.screens.GameScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class DankDungeon extends Game {

    public static int V_WIDTH = 1024;
    public static int V_HEIGHT = 760;
    public static final float PPM = 100;

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
        log.log();
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
