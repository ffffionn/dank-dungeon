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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
        viewport = new FitViewport(game.V_WIDTH, game.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, game.batch);
        this.table = new Table();
        table.debug();
        table.setFillParent(true);

        ProgressBar.ProgressBarStyle style = new ProgressBar.ProgressBarStyle();

        Skin uiSkin = new Skin(Gdx.files.internal("ui/skin.json"), new TextureAtlas("ui/skin.atlas"));

        Pixmap block = new Pixmap(30, 30, Pixmap.Format.RGBA8888);
        block.setColor(Color.WHITE);
        block.fill();
        uiSkin.add("block", new Texture(block));

        style.background = uiSkin.newDrawable("block", uiSkin.getColor("grey"));
        style.knob = uiSkin.newDrawable("block", uiSkin.getColor("light-green"));
        style.knobBefore = uiSkin.newDrawable("block", uiSkin.getColor("dark-green"));


        this.progress = new ProgressBar(0f, 100f, 0.5f, false, style);
        progress.setSize(300, 20);
        table.add(progress).center().expandY().expandX();
        stage.addActor(table);
    }


    @Override
    public void show() {
        assetManager.load("ui/skin.json", Skin.class);
        assetManager.load("sounds/main-loop-100.ogg", Music.class);
        assetManager.load("sounds/main-loop-110.ogg", Music.class);
        assetManager.load("sounds/main-loop-120.ogg", Music.class);
        assetManager.load("sounds/main-loop-130.ogg", Music.class);
        assetManager.load("sounds/main-loop-140.ogg", Music.class);
        assetManager.load("sounds/main-loop-150.ogg", Music.class);
        assetManager.load("sounds/steps.wav", Music.class);
        assetManager.load("sounds/barrier.ogg", Music.class);

        assetManager.load("sounds/cast-spell.wav", Sound.class);
        assetManager.load("sounds/rat-pain.ogg", Sound.class);
        assetManager.load("sounds/rat-death.ogg", Sound.class);
        assetManager.load("sounds/scorpion-pain.wav", Sound.class);
        assetManager.load("sounds/scorpion-death.wav", Sound.class);
        assetManager.load("sounds/hero-death.wav", Sound.class);
        assetManager.load("sounds/hero-pain1.ogg", Sound.class);
        assetManager.load("sounds/hero-pain2.ogg", Sound.class);
        assetManager.load("sounds/hero-pain3.ogg", Sound.class);
        assetManager.load("sounds/hero-munch.ogg", Sound.class);
        assetManager.load("sounds/hero-heal.ogg", Sound.class);
        assetManager.load("sounds/hero-healed.ogg", Sound.class);
        assetManager.load("sounds/wolf-pain.wav", Sound.class);
        assetManager.load("sounds/wolf-death.wav", Sound.class);
        assetManager.load("textures/crosshair.png", Texture.class);
    }

    @Override
    public void render(float delta) {
        progress.setValue(assetManager.getProgress());
        stage.act(delta);
        stage.draw();
        if(assetManager.update()){
            dispose();
            game.setScreen(new GameScreen(game, assetManager));
        }
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
