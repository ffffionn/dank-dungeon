package com.test.test.scenes;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.test.test.screens.HighScoreScreen;

/**
 * A simple text input to take the player's name if they set a high score.
 */
public class Textbox implements Input.TextInputListener {

    private HighScoreScreen screen;
    private Table table;

    public Textbox(HighScoreScreen screen, Table table){
        this.screen = screen;
        this.table = table;
    }

    @Override
    public void input(String text) {
        // replace symbols not in our font charset
        String clean = text.replaceAll("[:€¬¦£]", "");
        if(clean.length() > 15){
            clean = clean.substring(0,14);
        }
        // reorder the scores with the new name
        screen.resetLeaderboard(clean);
    }

    @Override
    public void canceled() {
    }
}
