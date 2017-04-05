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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;
import com.test.test.models.Hero;
import com.test.test.models.Pickup;

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
    private ArrayMap<Pickup.Type, Image> powerIcons;
    private Array<Pickup.Type> activePowers;

    private int score;
    private int floor;
    private int playerHealth;
    private int playerMana;

    private Sprite overlay;
    private Interpolation interpolation;

    private float alpha;
    private float flashDuration;
    private boolean flashing = false;
    private float flashTimer = 0.0f;


    public GameHud(SpriteBatch sb, Skin skin){
        this.score = 0;
        this.floor = 1;
        this.activePowers = new Array<Pickup.Type>();
        this.playerHealth = 100;
        this.playerMana = 100;
        this.viewport = new StretchViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        this.stage = new Stage(viewport, sb);
        this.skin = skin;

        this.interpolation = Interpolation.pow3Out;

        Pixmap pix = new Pixmap(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, Pixmap.Format.RGBA8888);
        pix.setColor(skin.getColor("white"));
        pix.fillRectangle(0, 0, DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT);
        this.overlay = new Sprite(new Texture(pix));
        overlay.setBounds(0, 0, DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT);
        overlay.setPosition(-100,-100);
        this.alpha = 0.0f;
        overlay.setAlpha(alpha);

        addStats();
        addBars();
        addIcons();

        stage.getRoot().getColor().a = 0;
        stage.getRoot().addAction(fadeIn(0.8f));
    }

    public void addPower(Pickup.Type type){
        if(activePowers.contains(type, true)){
            return;
        }
        activePowers.add(type);
        Image newIcon = powerIcons.get(type);
        newIcon.setSize(64, 64);
        newIcon.setPosition(DankDungeon.V_WIDTH - (74 * activePowers.size), 10);
        stage.addActor(newIcon);
        newIcon.getColor().a = 0;
        newIcon.addAction(Actions.fadeIn(0.3f));
    }

    public void deactivatePower(Pickup.Type t){
        activePowers.removeValue(t, true);
        powerIcons.get(t).addAction(Actions.fadeOut(0.3f));
    }

    public void update(float dt){
        stage.act(dt);

        if(flashing){
            flashTimer += dt;
            if (flashTimer >= flashDuration || alpha < 0.0f){
                alpha = 0.0f;
                flashing = false;
            }else{
                alpha = interpolation.apply(flashTimer / flashDuration);
                // don't let it get too solid
                if (alpha > 0.5f){
                    alpha = 1 - alpha;
                }
            }
            overlay.setAlpha(alpha);
        }
    }

    public void draw(SpriteBatch batch){
        overlay.draw(batch);
    }

    public void flash(Color color, Interpolation i, float duration) {
        if(flashing && (color != Color.GOLD)) return;

        overlay.setColor(color);
        interpolation = i;
        flashDuration = duration;
        flashTimer = 0.0f;
        flashing = true;
    }

    public void setFloor(int floor) {
        this.floor = floor;
        floorLabel.setText(String.format("FLOOR: %03d", this.floor));
        // flash the text slightly darker on update
        floorLabel.addAction(Actions.sequence(
                color(skin.getColor("deep-blue"), 0.25f, Interpolation.pow2In),
                color(skin.getColor("blue"), 0.25f, Interpolation.pow2Out)));
        flash(skin.getColor("alpha-blue"), Interpolation.circleOut, 0.3f);
    }

    public void updateHealth(int newHealth){
        if (newHealth < playerHealth){
            flash(skin.getColor("red"), Interpolation.pow3Out, Hero.INVINCIBILITY_TIMER);
        }
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
        // flash the score slightly darker on update
        scoreLabel.addAction(Actions.sequence(
                color(skin.getColor("deep-blue"), 0.25f, Interpolation.pow2In),
                color(skin.getColor("blue"), 0.25f, Interpolation.pow2Out)));
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

        healthBar = new ProgressBar(0, 100, 1.0f, false, barStyle);
        healthBar.setPosition(10, 50);
        healthBar.setAnimateDuration(0.5f);
        healthBar.setSize(300, healthBar.getPrefHeight());
        healthBar.setValue(playerHealth);

        barStyle = new ProgressBar.ProgressBarStyle();
        barStyle.background = skin.newDrawable("block", skin.getColor("blue"));
        barStyle.knobAfter = skin.newDrawable("block", skin.getColor("grey"));

        manaBar = new ProgressBar(0, 100, 0.5f, false, barStyle);
        manaBar.setPosition(10, 10);
        manaBar.setAnimateDuration(0.5f);
        manaBar.setSize(300, manaBar.getPrefHeight());
        manaBar.setValue(playerMana);

        stage.addActor(healthBar);
        stage.addActor(manaBar);
    }

    private void addIcons(){
        powerIcons = new ArrayMap<Pickup.Type, Image>();
        powerIcons.put(Pickup.Type.UNLIMITED_MANA, new Image(skin, "icon_mana"));
        powerIcons.put(Pickup.Type.FREEZE, new Image(skin, "icon_ice"));
        powerIcons.put(Pickup.Type.MULTI_FIRE, new Image(skin, "icon_multifire"));
        powerIcons.put(Pickup.Type.BOUNCING_BULLETS, new Image(skin, "icon_bounce"));
        powerIcons.put(Pickup.Type.DOUBLE_DMG, new Image(skin, "icon_damage"));
        powerIcons.put(Pickup.Type.INVINCIBLE, new Image(skin, "icon_invincible"));
    }
}
