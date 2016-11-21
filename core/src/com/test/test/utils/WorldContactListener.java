package com.test.test.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;


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
                System.out.println(fa.toString());
            }else if(fa.getUserData().equals("fireball") && fb.getUserData().equals("enemy")) {
                System.out.println(fa.toString());
            }else{
                System.out.println("....");

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
