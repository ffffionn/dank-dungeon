package com.test.test.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;
import com.test.test.models.Hero;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.color;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn;

/**
 * The game HUD. Displays HP/MP bars, score, dungeon level and active power-ups.
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

    public GameHud(SpriteBatch sb, Skin skin){
        this.score = 0;
        this.floor = 1;
        this.playerHealth = Hero.MAX_HEALTH;
        this.playerMana = Hero.MAX_MANA;
        this.viewport = new StretchViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        this.stage = new Stage(viewport, sb);
        this.skin = skin;

        // TODO: flash red
        Pixmap pix = new Pixmap(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, Pixmap.Format.RGBA8888);
        pix.setColor(skin.getColor("red"));
        Texture red = new Texture(pix);
        this.overlay = new Sprite(red);
        this.alpha = 0.4f;

        addStats();
        addBars();

        stage.getRoot().getColor().a = 0;
        stage.getRoot().addAction(fadeIn(0.8f));
    }

    public void update(float dt){
        stage.act(dt);
    }

    public void draw(SpriteBatch batch){
        overlay.setColor(skin.getColor("red"));
        overlay.draw(batch);
    }

    public void setFloor(int floor) {
        this.floor = floor;
        floorLabel.setText(String.format("FLOOR: %03d", this.floor));

//        floorLabel.addAction(Actions.sequence(
//                color(skin.getColor("light-green"), 0.2f, Interpolation.pow2In),
//                color(skin.getColor("alpha-blue"), 0.2f, Interpolation.pow2Out)));
//        floorLabel.getColor().a = 0;
//        floorLabel.addAction(fadeOut(1.0f));
//        floorLabel.addAction(fadeIn(1.0f));

    }

    public void updateHealth(int newHealth){
        playerHealth = newHealth;
        healthBar.setValue(playerHealth);
    }

    public void updateMana(int newMana){
        playerMana = newMana;
        manaBar.setValue(playerMana);
    }

    public void updateScore(int adjustment){
        score += adjustment;
        scoreLabel.setText(String.format("SCORE: %06d", score));
    }

    public void resize(int width, int height){
        viewport.update(width, height);
    }

    public int getScore(){
        return this.score;
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

        scoreLabel = new Label(String.format("SCORE: %06d", score), skin, "ui-label");
        floorLabel = new Label(String.format("FLOOR: %03d", floor), skin, "ui-label");

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

        // reverse the background and knob to get the bar to reach 0 properly
        barStyle.background = skin.newDrawable("block", skin.getColor("red"));
        barStyle.knobAfter = skin.newDrawable("block", skin.getColor("grey"));

        healthBar = new ProgressBar(0, Hero.MAX_HEALTH, 1.0f, false, barStyle);
        healthBar.setPosition(10, 50);
        healthBar.setAnimateDuration(0.5f);
        healthBar.setSize(300, healthBar.getPrefHeight());
        healthBar.setValue(playerHealth);

        barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = skin.newDrawable("block", skin.getColor("blue"));
        barStyle.knobAfter = skin.newDrawable("block", skin.getColor("grey"));

        manaBar = new ProgressBar(0, Hero.MAX_MANA, 0.5f, false, barStyle);
        manaBar.setPosition(10, 10);
        manaBar.setAnimateDuration(0.5f);
        manaBar.setSize(300, manaBar.getPrefHeight());
        manaBar.setValue(playerMana);

        stage.addActor(healthBar);
        stage.addActor(manaBar);
    }
}
