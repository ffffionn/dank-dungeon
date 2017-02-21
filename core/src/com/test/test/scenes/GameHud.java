package com.test.test.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
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
    private Table table;
    private Viewport viewport;
    private Label healthLabel;
    private Label scoreLabel;
    private Label floorLabel;

    private ProgressBar healthBar;
    private ProgressBar manaBar;
    private TextureRegion healthBarBackground;
    private TextureRegion healthBarForeground;

    private int score;
    private int floor;
    private int playerHealth;
    private int playerMana;

    private Image healthBG;
    private Image healthFG;

    protected static TextureAtlas uiAtlas;


    public GameHud(SpriteBatch sb, TextureAtlas atlas){
        uiAtlas = atlas;
        score = 0;
        floor = 1;
        playerHealth = Hero.MAX_HEALTH;
        playerMana = Hero.MAX_MANA;
        viewport = new FitViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        table = new Table();
        table.setFillParent(true);

        healthBG = new Image(uiAtlas.findRegion("empty_bar"));
        healthFG = new Image(uiAtlas.findRegion("healthbar"));

        skin = new Skin(Gdx.files.internal("ui/skin.json"));

        BitmapFont font =  skin.get("default-font", BitmapFont.class);

        healthBarBackground = new TextureRegion(new Texture("ui/empty_bar.png"));
        healthBarBackground.setRegion(20, 20, 200, 20);

        // create the health and mana bars
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle(
                new TextureRegionDrawable(uiAtlas.findRegion("empty_bar")),
                new TextureRegionDrawable(uiAtlas.findRegion("healthbar")));
        barStyle.knobBefore = barStyle.knob;
        barStyle.knobAfter = barStyle.knob;

        healthBar = new ProgressBar(0, Hero.MAX_HEALTH, 1.0f, false, barStyle);
        healthBar.setPosition(10, 10);
        healthBar.setAnimateDuration(0.8f);
        healthBar.setSize(100, healthBar.getPrefHeight());
        healthBar.setValue(playerHealth);

        barStyle.knob = new TextureRegionDrawable(uiAtlas.findRegion("manabar"));

        manaBar = new ProgressBar(0, Hero.MAX_MANA, 0.5f, false, barStyle);
        manaBar.setPosition(10, 70);
        manaBar.setAnimateDuration(0.2f);
        manaBar.setSize(100, manaBar.getPrefHeight());
        manaBar.setValue(playerMana);

        healthLabel = new Label(String.format("HP: %03d", playerHealth), new Label.LabelStyle(font, Color.LIGHT_GRAY));
        scoreLabel = new Label(String.format("%06d", score), new Label.LabelStyle(font, Color.LIGHT_GRAY));
        floorLabel = new Label(String.format("Mana: %03d", 100), new Label.LabelStyle(font, Color.LIGHT_GRAY));

        Table stats = new Table();
        stats.setFillParent(true);
        stats.top();
        stats.add(healthLabel).expandX().padTop(5);
        stats.add(scoreLabel).expandX().padTop(5);
        stats.add(floorLabel).expandX().padTop(5);

        Table bars = new Table();
        bars.setFillParent(true);
//        bars.bottom().left().add(healthBG).width(200).padTop(5);
//        bars.bottom().left().add(healthFG).width(200).padTop(5);
        bars.bottom().left().add(healthBar).width(200).padTop(5);
        bars.bottom().left().add(manaBar).width(200).padTop(5);

        table.add(stats);
//        table.add(bars);

        table.setDebug(true);
        stats.setDebug(true);
        bars.setDebug(true);

        VerticalGroup info = new VerticalGroup();
        info.align(Align.bottomLeft);
        info.bottom().addActor(healthBar);
        info.bottom().addActor(manaBar);

        stage.addActor(stats);
//        stage.addActor(bars);
//        stage.addActor(info);
//        stage.addActor(healthBar);
//        stage.addActor(manaBar);
        stage.addActor(table);
    }

    public void update(float dt){
//        healthBar.clamp(playerHealth);
        healthBar.act(dt);
        healthFG.setScale(playerHealth / ((float) Hero.MAX_HEALTH), 1);
    }

    public void draw(Batch batch){
        healthBG.draw(batch, 0.0f);
        healthFG.draw(batch, 0.0f);
        healthBar.draw(batch, 0.0f);
    }

    public Stage getStage(){ return stage; }

    public void setFloor(int floor) {
        this.floor = floor;
        floorLabel.setText(String.format("Floor: %03d", this.floor));
    }

    public void updateHealth(int newHealth){
        playerHealth = newHealth;
        healthLabel.setText(String.format("HP: %03d", playerHealth));
        healthBar.setValue(playerHealth);
    }

    public void updateMana(int newMana){
        playerMana = newMana;
        healthLabel.setText(String.format("Mana: %03d", playerHealth));
//        healthBar.setValue(playerMana);
    }

    public void updateScore(int adjustment){
        score += adjustment;
        scoreLabel.setText(String.format("%06d", score));
    }

    public void updatePlayerMana(int newMana){
        floorLabel.setText(String.format("Mana: %03d", newMana));
    }

    public void dispose(){
        stage.dispose();
    }

}
