package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;


/**
 * A simple screen displaying "GAME OVER".
 */
public class GameOverScreen implements Screen{

    private DankDungeon game;
    private Viewport viewport;
    private Stage stage;
    private AssetManager assetManager;
    private Table table;

    private int score;

    public GameOverScreen(DankDungeon game, int score, AssetManager manager){
        this.game = game;
        this.assetManager = manager;
        this.score = score;
        viewport = new FitViewport(game.V_WIDTH, game.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, game.batch);

        Skin skin = assetManager.get("ui/skin.json", Skin.class);
        Label.LabelStyle defaultStyle = new Label.LabelStyle(skin.getFont("default-font"), skin.getColor("red"));

        this.table = new Table();
        table.setFillParent(true);

        Label gameOverLabel = new Label("GAME OVER", defaultStyle);
        table.add(gameOverLabel).expandX().expandY().center();

        stage.addActor(table);
    }


    @Override
    public void show() {
        stage.getRoot().getColor().a = 0;
        stage.addAction(Actions.sequence(
                Actions.fadeIn(1.0f),
                Actions.delay(2.5f),
                Actions.fadeOut(1.0f),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        dispose();
                        game.setScreen(new HighScoreScreen(game, score, assetManager));
                        return true;
                    }
                }
        ));
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        draw();
    }

    public void draw(){
        Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.end();
        stage.draw();
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
        stage.dispose();
    }
}
