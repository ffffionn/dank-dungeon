package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;

/**
 * Created by Fionn on 02/04/2017.
 */
public class HelpScreen implements Screen {

    private DankDungeon game;
    private AssetManager assetManager;

    private Viewport viewport;
    private Stage stage;
    private Table table;

    public HelpScreen(DankDungeon game, AssetManager manager){
        this.game = game;
        this.assetManager = manager;

        this.viewport = new FitViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        this.stage = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(stage);
        this.table = new Table();
        table.setFillParent(true);
        table.debug();

        setTable();

        stage.addActor(table);
    }

    private void setTable(){
        table.clear();
        Skin skin = assetManager.get("ui/ui_skin.json", Skin.class);

        Label title = new Label("Help", skin, "title");

        table.add(title).expandX().expandY();
        table.row();
        
        table.row();
        table.add(new Label("Click anywhere to return to the Main Menu", skin, "monospaced-green")).expandX().expandY();
        table.padBottom(20.0f);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
        if(Gdx.input.isTouched()){
            stage.addAction(Actions.sequence(
                    Actions.fadeOut(1.5f),
                    new Action() {
                        @Override
                        public boolean act(float delta) {
                            dispose();
                            game.setScreen(new MainMenuScreen(game, assetManager));
                            return true;
                        }
                    }
            ));
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
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
