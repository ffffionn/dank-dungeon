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
public class Hud {

    public Stage stage;
    public Viewport viewport;

    private Integer timeCount;
    private Integer score;
    private Integer playerHealth;

    Label scoreLabel;
    Label timeLabel;
    Label healthLabel;

    public Hud(SpriteBatch sb){
        score = 0;
        playerHealth = 100;

        viewport = new FitViewport(SpaceAnts.V_WIDTH, SpaceAnts.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, sb);

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        scoreLabel = new Label(String.format("%06d", score), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        healthLabel = new Label(String.format("%03d", playerHealth), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        timeLabel = new Label(String.format("%04d", timeCount), new Label.LabelStyle(new BitmapFont(), Color.WHITE));



    }

}
