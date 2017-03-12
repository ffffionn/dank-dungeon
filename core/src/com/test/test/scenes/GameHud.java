package com.test.test.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;
import com.test.test.models.Hero;

/**
 * The game HUD.
 */
public class GameHud {

    // scene2d objects
    private Skin skin;
    private Stage stage;
    private Viewport viewport;

    // HUD elements
    private Label scoreLabel;
    private Label floorLabel;
    private ProgressBar healthBar;
    private ProgressBar manaBar;

    private int score;
    private int floor;
    private int playerHealth;
    private int playerMana;

    private Sprite overlay;
    private float alpha;

    public GameHud(SpriteBatch sb){
        score = 0;
        floor = 1;
        playerHealth = Hero.MAX_HEALTH;
        playerMana = Hero.MAX_MANA;
        viewport = new StretchViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        skin = new Skin(Gdx.files.internal("ui/skin.json"));
        Pixmap pix = new Pixmap(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, Pixmap.Format.RGBA8888);
        pix.setColor(skin.getColor("red"));
        Texture red = new Texture(pix);
        overlay = new Sprite(red);
        alpha = 1.0f;

        addStats();
        addBars();
    }

    public void update(float dt){
        healthBar.act(dt);
        manaBar.act(dt);
    }

    public void setFloor(int floor) {
        this.floor = floor;
        floorLabel.setText(String.format("FLOOR: %03d", this.floor));
    }

    public void updateHealth(int newHealth){
        playerHealth = newHealth;
        healthBar.setValue(playerHealth);
    }

    public void updateMana(int newMana){
        playerMana = newMana;
        manaBar.setValue(playerMana);
    }

    public void draw(SpriteBatch batch){
        overlay.draw(batch);
    }

    public void resize(int width, int height){
        viewport.update(width, height);
    }

    public void updateScore(int adjustment){
        score += adjustment;
        scoreLabel.setText(String.format("SCORE: %06d", score));
    }

    public void dispose(){
        stage.dispose();
    }

    public Stage getStage(){ return stage; }

    private void addStats(){
        Table table = new Table();
        table.setFillParent(true);
//        table.setDebug(true);
        BitmapFont font =  skin.get("default-font", BitmapFont.class);

        scoreLabel = new Label(String.format("SCORE: %06d", score), skin, "score-label");
        floorLabel = new Label(String.format("FLOOR: %03d", floor), skin, "floor-label");

        table.top().add(scoreLabel).height(20).expandX().padTop(5);
        table.add().expandX().padTop(5);
        table.add(floorLabel).expandX().padTop(5);
        table.row();

        stage.addActor(table);
    }

    private void addBars(){
        // make a white block so we can tint it as we need
        Pixmap block = new Pixmap(30, 30, Pixmap.Format.RGBA8888);
        block.setColor(Color.WHITE);
        block.fill();
        skin.add("block", new Texture(block));

        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = skin.newDrawable("block", skin.getColor("grey"));
        barStyle.knobBefore = skin.newDrawable("block", skin.getColor("red"));

        healthBar = new ProgressBar(0, Hero.MAX_HEALTH, 1.0f, false, barStyle);
        healthBar.setPosition(10, 50);
        healthBar.setAnimateDuration(0.5f);
        healthBar.setSize(200, healthBar.getPrefHeight());
        healthBar.setValue(playerHealth);

        barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = skin.newDrawable("block", skin.getColor("grey"));
        barStyle.knobBefore = skin.newDrawable("block", skin.getColor("blue"));

        manaBar = new ProgressBar(0, Hero.MAX_MANA, 0.5f, false, barStyle);
        manaBar.setPosition(10, 10);
        manaBar.setAnimateDuration(0.5f);
        manaBar.setSize(200, manaBar.getPrefHeight());
        manaBar.setValue(playerMana);

        stage.addActor(healthBar);
        stage.addActor(manaBar);
    }

}
