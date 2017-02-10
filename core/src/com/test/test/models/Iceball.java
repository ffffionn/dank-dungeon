package com.test.test.models;

import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;

/**
 * Created by Fionn on 09/02/2017.
 */
public class Iceball extends Projectile {

    public Iceball(GameScreen screen, Vector2 startPosition, Vector2 target){
        super(screen, startPosition, target);
        this.damageAmount = 15;
    }

}
