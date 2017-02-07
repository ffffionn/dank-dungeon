package com.test.test.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;
import com.test.test.models.Hero;

/**
 * The game HUD.
 */
public class GameHud {

    private Skin skin;
    private Stage stage;
    private Viewport viewport;
    private Label healthLabel;
    private Label scoreLabel;
    private Label floorLabel;

//    private ProgressBar healthBar;

    private TextureRegion healthBar;
    private TextureRegion healthBarBackground;

    private int score;
    private int floor;
    private int playerHealth;

    public GameHud(SpriteBatch sb){
        score = 0;
        playerHealth = Hero.MAX_HEALTH;
        floor = 1;
        viewport = new FitViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);

        Table table = new Table();
        table.bottom();
        table.setFillParent(true);

        skin = new Skin();
        skin = new Skin(Gdx.files.internal("ui/skin.json"));
        Pixmap pixmap = new Pixmap(10, 100, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        skin.add("font", new BitmapFont(Gdx.files.internal("fonts/segoe-ui.fnt")));

        healthBarBackground = new TextureRegion(new Texture(pixmap));
//        healthBarBackground = new TextureRegion(new Texture("textures/blank.jpg"));
        skin.add("healthBarBg", new Texture(pixmap));
        Drawable hb = skin.newDrawable("white", Color.DARK_GRAY);


        // create the health and mana bars
//        TextureRegionDrawable textureBar = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("textures/health.png"))));
//
//        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle(skin.newDrawable("white", Color.DARK_GRAY), textureBar);
//        barStyle.knobBefore = barStyle.knob;
//        barStyle.knobAfter = barStyle.knob;
//        healthBar = new ProgressBar(0, Hero.MAX_HEALTH, 1.0f, false, barStyle);
//        healthBar.setPosition(100, 10);
//        healthBar.setSize(150, healthBar.getPrefHeight());
//        stage.addActor(healthBar);
//        healthBar.setValue(playerHealth);

        FileHandle font = Gdx.files.internal("fonts/segoe-ui.fnt");

        healthLabel = new Label(String.format("HP: %03d", playerHealth),
                new Label.LabelStyle(new BitmapFont(font), Color.LIGHT_GRAY));
        floorLabel = new Label(String.format("Level: %03d", floor), new Label.LabelStyle(new BitmapFont(font), Color.LIGHT_GRAY));
        scoreLabel = new Label(String.format("%06d", score), new Label.LabelStyle(new BitmapFont(font), Color.LIGHT_GRAY));

        table.add(healthLabel).expandX().padBottom(5);
        table.add(scoreLabel).expandX().padBottom(5);
        table.add(floorLabel).expandX().padBottom(5);

        table.setDebug(true);
        table.left();
        stage.addActor(table);
    }


    public Stage getStage(){ return stage; }

    public void setFloor(int floor) {
        this.floor = floor;
        floorLabel.setText(String.format("Floor: %03d", this.floor));
    }

    public void updatePlayerHealth(int newHealth){
        playerHealth = newHealth;
        healthLabel.setText(String.format("Health: %03d", playerHealth));
//        healthBar.setValue(playerHealth);
//        healthBar.act(0.2f);
    }

    public void updateScore(int adjustment){
        score += adjustment;
        scoreLabel.setText(String.format("%06d", score));
    }

    public void dispose(){
        stage.dispose();
    }

}
