package com.test.test.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.models.Enemy;
import com.test.test.models.Fireball;


/**
 * Created by Fionn on 21/11/2016.
 */
public class WorldContactListener implements ContactListener{

    private World world;

    public WorldContactListener(World world){
        this.world = world;
    }

    public void beginContact(Contact c){
        Fixture fa = c.getFixtureA();
        Fixture fb = c.getFixtureB();


        if(fa.getUserData() != null && fb.getUserData() != null){
            if(fa.getUserData().equals("player") && fb.getUserData().equals("enemy") ||
                (fa.getUserData().equals("enemy") && fb.getUserData().equals("player"))){
                Vector2 v = fa.getBody().getLinearVelocity();
                fa.getBody().setLinearVelocity(new Vector2(-v.x * 2, -v.y * 2));
                fb.getBody().setLinearVelocity(new Vector2(v.x, v.y));
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
            }else{

            }

        }

    }

    // called when two fixtures no longer collide
    public void endContact(Contact c) {

        Fixture fa = c.getFixtureA();
        Fixture fb = c.getFixtureB();

    }

    public void preSolve(Contact c, Manifold m) {}
    public void postSolve(Contact c, ContactImpulse ci) {}
}
