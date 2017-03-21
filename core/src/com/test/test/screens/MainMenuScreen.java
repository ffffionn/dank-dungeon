package com.test.test.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;

/**
 * The Main Menu.
 */
public class MainMenuScreen implements Screen{

    private DankDungeon game;
    private AssetManager assetManager;
    private Viewport viewport;

    private Stage stage;
    private Table table;
    private Skin skin;

    public MainMenuScreen(DankDungeon game, AssetManager manager){
        this.game = game;
        this.assetManager = manager;

        this.viewport = new FitViewport(game.V_WIDTH, game.V_HEIGHT, new OrthographicCamera());
        this.stage = new Stage(viewport, game.batch);

        this.skin = assetManager.get("ui/skin.json", Skin.class);
        this.table = new Table();
        table.debug();
        table.setFillParent(true);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

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
