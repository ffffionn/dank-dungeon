package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;

/**
 * Loads up all the game sounds and textures into an AssetManager before entering the Main Menu.
 */
public class LoadingScreen implements Screen {

    private DankDungeon game;
    private AssetManager assetManager;
    private Viewport viewport;

    private Stage stage;
    private Table table;

    private ProgressBar progress;


    public LoadingScreen(DankDungeon game){
        this.game = game;
        this.assetManager = new AssetManager();
        viewport = new FitViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, game.batch);
        this.table = new Table();
//        table.debug();
        table.setFillParent(true);

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();

        Skin uiSkin = new Skin(Gdx.files.internal("ui/ui_skin.json"), new TextureAtlas("ui/ui_skin.pack"));

        Pixmap block = new Pixmap(30, 30, Pixmap.Format.RGBA8888);
        block.setColor(Color.WHITE);
        block.fill();
        uiSkin.add("block", new Texture(block));

        style.background = uiSkin.newDrawable("block", uiSkin.getColor("grey"));
        style.knob = uiSkin.newDrawable("block", uiSkin.getColor("yellow-green"));
        style.knobBefore = uiSkin.newDrawable("block", uiSkin.getColor("yellow-green"));

        Label.LabelStyle defaultStyle = uiSkin.get("monospaced-green", Label.LabelStyle.class);
        Label loadingText = new Label("LOADING..", defaultStyle);
        table.padTop(Value.percentHeight(0.25f)).add(loadingText).center().expandX();
        table.row();

        this.progress = new ProgressBar(0.0f, 1.0f, 0.001f, false, style);
        progress.setSize(600, 30);
        progress.setAnimateDuration(0.2f);
        table.add(progress).center().expandY().expandX();
        stage.addActor(table);
    }


    @Override
    public void show() {
        assetManager.load("ui/ui_skin.json", Skin.class);

        assetManager.load("sounds/main-loop-100.ogg", Music.class);
        assetManager.load("sounds/main-loop-110.ogg", Music.class);
        assetManager.load("sounds/main-loop-120.ogg", Music.class);
        assetManager.load("sounds/main-loop-130.ogg", Music.class);
        assetManager.load("sounds/main-loop-140.ogg", Music.class);
        assetManager.load("sounds/main-loop-150.ogg", Music.class);
        assetManager.load("sounds/steps.wav", Music.class);
        assetManager.load("sounds/barrier.ogg", Music.class);
        assetManager.load("sounds/a-journey-awaits.ogg", Music.class);

        assetManager.load("sounds/cast-spell.wav", Sound.class);
        assetManager.load("sounds/rat-pain.ogg", Sound.class);
        assetManager.load("sounds/rat-death.ogg", Sound.class);
        assetManager.load("sounds/scorpion-pain.wav", Sound.class);
        assetManager.load("sounds/scorpion-death.wav", Sound.class);
        assetManager.load("sounds/hero-death.wav", Sound.class);
        assetManager.load("sounds/hero-powerup.wav", Sound.class);
        assetManager.load("sounds/hero-pain1.ogg", Sound.class);
        assetManager.load("sounds/hero-pain2.ogg", Sound.class);
        assetManager.load("sounds/hero-pain3.ogg", Sound.class);
        assetManager.load("sounds/hero-munch.ogg", Sound.class);
        assetManager.load("sounds/hero-heal.ogg", Sound.class);
        assetManager.load("sounds/hero-healed.ogg", Sound.class);
        assetManager.load("sounds/wolf-pain.wav", Sound.class);
        assetManager.load("sounds/wolf-death.wav", Sound.class);
        assetManager.load("textures/worldTextures.pack", TextureAtlas.class);
        assetManager.load("ui/ui_skin.pack", TextureAtlas.class);

        // newCursor only takes Pixmap which can only be made directly from file
        assetManager.load("textures/crosshair.png", Pixmap.class);
    }

    @Override
    public void render(float delta) {
        // update the loading bar
        progress.setValue(assetManager.getProgress());
        stage.act(delta);

        // draw the progress
        stage.draw();

        // if done, transition to MainMenuScreen
        if(assetManager.update()){
            stage.addAction(Actions.sequence(
                Actions.fadeOut(1.0f),
                new Action() {
                    @Override
                    public boolean act(float delta) {
                        dispose();
//                        game.setScreen(new MainMenuScreen(game, assetManager));
                        game.setScreen(new GameScreen(game, assetManager));
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
        stage.dispose();
    }
}
