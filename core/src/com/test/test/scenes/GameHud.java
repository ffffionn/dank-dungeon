package com.test.test.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.SpaceAnts;

/**
 * Created by Fionn on 22/10/2016.
 */
public class GameHud {

    private Stage stage;
    private Viewport viewport;
    private Label healthLabel;
    private Label scoreLabel;
    private Label floorLabel;

    private int score;
    private int floor;
    private int playerHealth;

    public GameHud(SpriteBatch sb){
        score = 0;
        playerHealth = 100;
        floor = 1;
        viewport = new FitViewport(SpaceAnts.V_WIDTH, SpaceAnts.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);

        Table table = new Table();
        table.bottom();
        table.setFillParent(true);

        healthLabel = new Label(String.format("Health: %03d", playerHealth), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        floorLabel = new Label(String.format("Floor: %03d", floor), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        scoreLabel = new Label(String.format("%06d", score), new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        table.add(floorLabel).expandX().padBottom(5);
        table.add(scoreLabel).expandX().padBottom(5);
        table.add(healthLabel).expandX().padBottom(5);

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
    }

    public void updateScore(int adjustment){
        score += adjustment;
        scoreLabel.setText(String.format("%06d", score));
    }
}
