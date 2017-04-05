package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;

import static com.test.test.screens.GameScreen.TILE_SIZE;

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

        setTable();

        stage.addActor(table);
    }

    private void setTable(){
        table.clear();
        Skin skin = assetManager.get("ui/ui_skin.json", Skin.class);

        Label title = new Label("Help", skin, "title");

        table.padTop(10.0f);
        table.add(title).expandX();
        table.row();

        TextureRegion[][] items = assetManager.get("textures/worldTextures.pack", TextureAtlas.class).findRegion("items").split(25, 25);

        Image healthBottle = new Image(items[2][1]);
        Image manaBottle = new Image(items[2][2]);
        Image chili = new Image(items[5][4]);
        Image cheese = new Image(items[5][3]);
        Image whiteCheese = new Image(items[5][6]);
        Image pear = new Image(items[5][2]);
        Image mushrooms = new Image(items[5][7]);
        Image ham = new Image(items[6][0]);
        Image staff = new Image(items[1][1]);
        Image boots = new Image(items[4][0]);
        Image shield = new Image(items[1][0]);
        Image goal = new Image(assetManager.get("textures/worldTextures.pack", TextureAtlas.class).findRegion("tiles-blue").split(TILE_SIZE, TILE_SIZE)[1][7]);

        Table t = new Table();

        t.add(healthBottle).colspan(3);
        t.add(manaBottle).colspan(3);
        t.add();
        t.add(new Label("Use potions to stay alive!", skin, "monospaced-light")).expandX().pad(20.0f);
        t.row();

        t.add(goal).colspan(7);
        t.add(new Label("Find the stairs on \n each floor to progress", skin, "monospaced-light")).expandX().pad(10.0f);
        t.row();

        t.add(staff).colspan(2);
        t.add(shield).colspan(2);
        t.add(boots).colspan(2);
        t.add();
        t.add(new Label("Collect items to permanently \n boost stats", skin, "monospaced-light")).expandX().pad(10.0f);
        t.row();

        t.add(cheese);
        t.add(whiteCheese);
        t.add(chili);
        t.add(ham);
        t.add(pear);
        t.add(mushrooms);
        t.add();
        t.add(new Label("Eat foods to augment your spells", skin, "monospaced-light")).expandX().pad(10.0f);
        t.row();



        table.add(t).expandX().expandY();

        table.row();
        table.add(new Label("Click anywhere to return to the Main Menu", skin, "monospaced-green")).expandX();
        table.padBottom(20.0f);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        draw();

        if(Gdx.input.isTouched()){
            stage.addAction(Actions.sequence(
                    Actions.fadeOut(0.3f),
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


    public void draw(){
        Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.end();
        stage.draw();
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
