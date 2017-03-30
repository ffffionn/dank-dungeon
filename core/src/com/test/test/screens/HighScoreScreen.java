package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.DankDungeon;
import com.test.test.scenes.Textbox;

import static com.badlogic.gdx.Gdx.input;

/**
 * Displays the top ten high scores set.
 */
public class HighScoreScreen implements Screen {

    private DankDungeon game;
    private Viewport viewport;
    private Stage stage;
    private AssetManager assetManager;
    private Table table;
    private int newScore;

    private Array<String> highScores;
    private boolean highScoreSet;

    private FileHandle scoreFile;

    public HighScoreScreen(DankDungeon game, int score, AssetManager manager){
        this.game = game;
        this.assetManager = manager;
        viewport = new FitViewport(game.V_WIDTH, game.V_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, game.batch);
        this.newScore = score;

        Skin skin = assetManager.get("ui/skin.json", Skin.class);
        Label.LabelStyle defaultStyle = new Label.LabelStyle(skin.getFont("default-font"), skin.getColor("red"));
        Label.LabelStyle monoStyle = skin.get("monospaced", Label.LabelStyle.class);

        this.table = new Table();
//        table.debug();
        table.setFillParent(true);

        highScores = new Array<String>();

        // get the highscores from file
        scoreFile = Gdx.files.local("data/highscores.txt");
        if (scoreFile.length() == 0) {
            createBlankScores();
        }else{
            String text = scoreFile.readString();
            highScores.addAll(text.split("\n"));
        }

        try{
            int lowestScore = Integer.parseInt(highScores.get(highScores.size - 1).split(":")[1].trim());
            this.highScoreSet = score > lowestScore;
        }catch(Exception e){
            e.printStackTrace();
            highScoreSet = false;
            createBlankScores();
        }

        setTable();

        stage.addActor(table);
    }

    private void createBlankScores(){
        highScores.clear();
        for(int i = 0; i < 10; i++){
            highScores.add(String.format("%10s:%d", "-", 0));
        }
        saveHighScores();
    }

    private void saveHighScores(){
        String text = "";
        // assemble text
        for(String s : highScores){
            text += (s + "\n");
        }

        scoreFile.writeString(text, false);
    }

    public void resetLeaderboard(String name){
        Array<Integer> intScores = new Array<Integer>();
        for( String s : highScores){
            intScores.add(Integer.parseInt(s.split(":")[1].trim()));
        }


        // remove the lowest score and add the new one
        intScores.pop();
        intScores.add(newScore);
        // sort the scores, then reverse for descending order
        intScores.sort();
        intScores.reverse();
        // get the player's position in the top ten
        int pos = intScores.indexOf(newScore, false);
        highScores.pop();
        highScores.insert(pos, String.format("%10s:%d", name, newScore));

        saveHighScores();
        setTable();
    }

    private void setTable(){
        table.clear();
        Skin skin = assetManager.get("ui/skin.json", Skin.class);
        Label.LabelStyle defaultStyle = skin.get("game-over", Label.LabelStyle.class);
        Label.LabelStyle monoStyle = skin.get("monospaced", Label.LabelStyle.class);

        String labelString;
        if(newScore >= 0){
            if(highScoreSet){
                labelString = String.format("NEW HIGH SCORE: %06d", newScore);
            }else{
                labelString = String.format("YOUR SCORE: %06d", newScore);
            }
        }else{
            labelString = "HIGHSCORES";
            defaultStyle = new Label.LabelStyle(skin.getFont("default-font"), skin.getColor("green"));
        }
        Label topLabel= new Label(labelString, defaultStyle);


        table.add(topLabel).expandX().expandY().center().padTop(10.0f);
        table.row();


        int position = 1;
        String scoreString;
        for(String s : highScores){
            scoreString = String.format("%02d  %15s  %06d",
                    position++,
                    s.split(":")[0],
                    Integer.parseInt(s.split(":")[1].trim()));

            table.row();
            table.add(new Label(scoreString, monoStyle)).expandX();
        }
        table.padBottom(20.0f);

        if(newScore >= 0){
            table.row();
            table.add(new Label("Click anywhere to play again", skin.get("monospaced-green", Label.LabelStyle.class))).expandX().expandY();
            table.padBottom(20.0f);
        }

    }

    @Override
    public void show() {
        if(highScoreSet){
            Input.TextInputListener listener = new Textbox(this, table);
            input.getTextInput(listener, "NEW HIGH SCORE!", "", "Enter Your Name");
        }
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        draw();
        // if this is a game over, rather than from the main menu
        if( Gdx.input.isTouched() ){
            if(newScore >= 0){
                stage.addAction(Actions.sequence(
                        Actions.fadeOut(1.5f),
                        new Action() {
                            @Override
                            public boolean act(float delta) {
                                dispose();
                                game.setScreen(new GameScreen(game, assetManager));
                                return true;
                            }
                        }
                ));
            }else{
//                game.setScreen(new MainMenuScreen(game, assetManager));
            }
        }
    }

    public void draw(){
        Gdx.gl.glClearColor(0f / 255f, 0f / 255f, 0f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();
        game.batch.end();
        stage.draw();
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
