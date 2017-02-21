package com.test.test.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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
    private Table table;
    private Viewport viewport;
    private Label healthLabel;
    private Label scoreLabel;
    private Label floorLabel;

    private ProgressBar healthBar;
    private TextureRegion healthBarBackground;
    private TextureRegion healthBarForeground;

    private int score;
    private int floor;
    private int playerHealth;
    private int playerMana;

    public GameHud(SpriteBatch sb){
        score = 0;
        playerHealth = Hero.MAX_HEALTH;
        playerMana = Hero.MAX_MANA;
        floor = 1;
        viewport = new FitViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);
        table = new Table();
        table.setFillParent(true);

        skin = new Skin();
        skin = new Skin(Gdx.files.internal("ui/skin.json"));
        Pixmap pixmap = new Pixmap(10, 100, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        skin.add("white", new Texture(pixmap));
        skin.add("font", new BitmapFont(Gdx.files.internal("fonts/segoe-ui.fnt")));
        BitmapFont font =  skin.get("default-font", BitmapFont.class);

//        healthBarBackground = new TextureRegion(new Texture(pixmap));
//        healthBarBackground = new TextureRegion(new Texture("textures/blank.jpg"));
        healthBarBackground = new TextureRegion(new Texture("ui/empty_bar.png"));
        healthBarBackground.setRegion(20, 20, 200, 20);

        // create the health and mana bars
        TextureRegionDrawable textureBar = new TextureRegionDrawable(new TextureRegion(
                new Texture(Gdx.files.internal("textures/health.png"))));
        ProgressBar.ProgressBarStyle barStyle = new ProgressBar.ProgressBarStyle(
                skin.newDrawable("white"), textureBar);
        barStyle.knobAfter = barStyle.knob;

        healthBar = new ProgressBar(0, Hero.MAX_HEALTH, 1.0f, false, barStyle);
        healthBar.setPosition(100, 10);
        healthBar.setAnimateDuration(1.2f);
        healthBar.setSize(150, healthBar.getPrefHeight());
        healthBar.setValue(playerHealth);

        healthLabel = new Label(String.format("HP: %03d", playerHealth), new Label.LabelStyle(font, Color.LIGHT_GRAY));
        scoreLabel = new Label(String.format("%06d", score), new Label.LabelStyle(font, Color.LIGHT_GRAY));
        floorLabel = new Label(String.format("Mana: %03d", 100), new Label.LabelStyle(font, Color.LIGHT_GRAY));
//        floorLabel = new Label(String.format("Level: %03d", floor), new Label.LabelStyle(font, Color.LIGHT_GRAY));

//        table.top();
//        table.add(healthBar).expandX().padTop(5);
//        table.row().expandX();

        table.bottom();
        table.add(healthLabel).expandX().padBottom(5);
        table.add(floorLabel).expandX().padBottom(5);
        table.add(scoreLabel).expandX().padBottom(5);

        table.setDebug(true);
        stage.addActor(table);
    }

    public void update(float dt){
        healthBar.act(dt);
    }

    public void draw(Batch batch){
        healthBar.draw(batch, 0.0f);
    }

    public Stage getStage(){ return stage; }

    public void setFloor(int floor) {
        this.floor = floor;
        floorLabel.setText(String.format("Floor: %03d", this.floor));
    }

    public void updateHealth(int newHealth){
        playerHealth = newHealth;
        healthLabel.setText(String.format("Health: %03d", playerHealth));
        healthBar.setValue(playerHealth);
    }

    public void updateMana(int newMana){
        playerMana = newMana;
        healthLabel.setText(String.format("Health: %03d", playerHealth));
        healthBar.setValue(playerMana);
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
