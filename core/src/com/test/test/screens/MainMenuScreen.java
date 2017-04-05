package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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

    private Music bgm;

    public MainMenuScreen(DankDungeon game, AssetManager manager){
        this.game = game;
        this.assetManager = manager;

        this.viewport = new FitViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        this.stage = new Stage(viewport, game.batch);

        // take input for button clicks
        Gdx.input.setInputProcessor(stage);

        this.skin = assetManager.get("ui/ui_skin.json");
        this.table = new Table();
        table.setFillParent(true);

        setTable();

        // play menu music
        this.bgm = assetManager.get("sounds/a-journey-awaits.ogg", Music.class);
        bgm.setLooping(true);
        bgm.setVolume(0.4f);
        bgm.play();
    }

    private void setTable(){
        table.clear();

        Label gameTitle = new Label("Dank Dungeon", skin, "title");

        TextButton playButton = new TextButton("Play!", skin);
        playButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.5f),
                    new Action() {
                        @Override
                        public boolean act(float delta) {
                            dispose();
                            bgm.stop();
                            game.setScreen(new GameScreen(game, assetManager));
                            return true;
                        }
                    }
                ));
            }
        });


        TextButton highScoreButton = new TextButton("High Scores", skin);
        highScoreButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                stage.addAction(Actions.sequence(
                        Actions.fadeOut(0.3f),
                        new Action() {
                            @Override
                            public boolean act(float delta) {
                                dispose();
                                // pass -1 as score as we're coming from menu and not game over
                                game.setScreen(new HighScoreScreen(game, -1, assetManager));
                                return true;
                            }
                        }
                ));
            }
        });

        TextButton helpButton = new TextButton("Help", skin);
        helpButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                stage.addAction(Actions.sequence(
                        Actions.fadeOut(0.3f),
                        new Action() {
                            @Override
                            public boolean act(float delta) {
                                dispose();
                                game.setScreen(new HelpScreen(game, assetManager));
                                return true;
                            }
                        }
                ));
            }
        });

        table.padTop(Value.percentHeight(0.05f));

        table.top();
        table.add(gameTitle).center().expandX().expandY().padBottom(20.0f);
        table.row();

        Table t = new Table();
        t.add(playButton).expandX().expandY().center().padBottom(20.0f);
        t.row();
        t.add(highScoreButton).expandX().expandY().center().padBottom(20.0f);
        t.row();
        t.add(helpButton).expandX().expandY().center();
        t.row();
        table.add(t).expandX().expandY().top();

        table.padBottom(Value.percentHeight(0.05f));


        stage.addActor(table);

    }

    public void draw(){
        Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.end();
        stage.draw();
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        draw();
    }

    @Override
    public void resize(int width, int height){
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
        stage.dispose();
    }
}
