package com.test.test.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.models.Enemy;
import com.test.test.models.Fireball;
import com.test.test.models.Hero;
import com.test.test.screens.GameScreen;


/**
 * Created by Fionn on 21/11/2016.
 */
public class WorldContactListener implements ContactListener{

    private GameScreen screen;

    public WorldContactListener(GameScreen screen){
        this.screen = screen;
    }

    public void beginContact(Contact c){
        Fixture fa = c.getFixtureA();
        Fixture fb = c.getFixtureB();

        if(fa.getUserData() != null && fb.getUserData() != null){
            if(fa.getUserData().equals("player") && fb.getUserData().equals("enemy") ) {
                Vector2 v = fa.getBody().getLinearVelocity();
                fa.getBody().setLinearVelocity(new Vector2(-v.x, -v.y));
                fb.getBody().setLinearVelocity(new Vector2(v.x, v.y));
                ((Hero) fa.getBody().getUserData()).damage(3);
            }else if(fa.getUserData().equals("enemy") && fb.getUserData().equals("player")){
                Vector2 v = fb.getBody().getLinearVelocity();
                fb.getBody().setLinearVelocity(new Vector2(-v.x, -v.y));
                fa.getBody().setLinearVelocity(new Vector2(v.x, v.y));
                ((Hero) fb.getBody().getUserData()).damage(3);
            }else if(fa.getUserData().equals("enemy") && fb.getUserData().equals("fireball")) {
                ((Enemy) fa.getBody().getUserData()).setToDestroy();
                ((Fireball) fb.getBody().getUserData()).setToDestroy();
            }else if(fa.getUserData().equals("fireball") && fb.getUserData().equals("enemy")) {
                ((Fireball) fa.getBody().getUserData()).setToDestroy();
                ((Enemy) fb.getBody().getUserData()).setToDestroy();
            }else if(fa.getUserData().equals("fireball") && fb.getUserData().equals("wall")) {
                ((Fireball) fa.getBody().getUserData()).setToDestroy();
            }else if(fa.getUserData().equals("wall") && fb.getUserData().equals("fireball")) {
                ((Fireball) fb.getBody().getUserData()).setToDestroy();
            }else if(fa.getUserData().equals("barrier") && fb.getUserData().equals("fireball")) {
                ((Fireball) fb.getBody().getUserData()).setToDestroy();
                System.out.println("SHIELD HIT A");
            }else if(fa.getUserData().equals("fireball") && fb.getUserData().equals("barrier")) {
                ((Fireball) fa.getBody().getUserData()).setToDestroy();
                System.out.println("SHIELD HIT B");
            }else if(fa.getUserData().equals("barrier") && fb.getUserData().equals("enemy")){
                Vector2 v = fb.getBody().getLinearVelocity();
                fb.getBody().setLinearVelocity(new Vector2(v.x * -5, v.y * -5));
                System.out.println("enemy B");
                ((Enemy) fb.getBody().getUserData()).stun();
            }else if(fb.getUserData().equals("barrier") && fa.getUserData().equals("enemy")){
                System.out.println("enemy A");
                Vector2 v = fa.getBody().getLinearVelocity();
                fa.getBody().setLinearVelocity(new Vector2(v.x * -5, v.y * -5));
                ((Enemy) fa.getBody().getUserData()).stun();
            }else if((fa.getUserData().equals("goal") && fb.getUserData().equals("player"))
                    || (fa.getUserData().equals("player") && fb.getUserData().equals("goal"))){
                screen.levelUp();
            }else{

            }

        }

    }

    // called when two fixtures no longer collide
    public void endContact(Contact c) {

        Fixture fa = c.getFixtureA();
        Fixture fb = c.getFixtureB();

//        if(fa.getUserData() != null && fb.getUserData() != null) {
//            if((fa.getUserData().equals("goal") && fb.getUserData().equals("player")) || (fa.getUserData().equals("player") && fb.getUserData().equals("goal"))){
//                screen.levelUp();
//            }else{
//
//            }
//        }

    }

    public void preSolve(Contact c, Manifold m) {}
    public void postSolve(Contact c, ContactImpulse ci) {}
}
