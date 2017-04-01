package com.test.test.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.models.*;
import com.test.test.screens.GameScreen;


/**
 * ContactListener for physics bodies in the game.
 */
public class WorldContactListener implements ContactListener{

    private GameScreen screen;

    // ENTITY COLLISION BITS
    public static final short PLAYER = 0x1;
    public static final short WALL = 0x2;
    public static final short ENEMY = 0x4;
    public static final short PLAYER_PROJECTILE = 0x8;
    public static final short ENEMY_PROJECTILE = 0x16;
    public static final short BARRIER = 0x32;
    public static final short PICKUP = 0x64;


    public WorldContactListener(GameScreen screen){
        this.screen = screen;
    }

    public void beginContact(Contact c){
        Fixture fa = c.getFixtureA();
        Fixture fb = c.getFixtureB();

        short a = fa.getFilterData().categoryBits;
        short b = fb.getFilterData().categoryBits;

        if(fa.getUserData() == null || fb.getUserData() == null) {
            return;
        }

        // handle projectile collisions
        if(a == PLAYER_PROJECTILE || a == ENEMY_PROJECTILE){
            ProjectileCollisionHandler.collide((Projectile) fa.getBody().getUserData(), fb.getBody());
        }else if(b == PLAYER_PROJECTILE || b == ENEMY_PROJECTILE){
            ProjectileCollisionHandler.collide((Projectile) fb.getBody().getUserData(), fa.getBody());
        }

        // handle barrier collisions
        if (a == BARRIER) {
            if (b == ENEMY) {
                EnemyCollisionHandler.collide(fb.getBody(), fa.getBody());
            }
        } else if (b == BARRIER) {
            if( a == ENEMY){
                EnemyCollisionHandler.collide(fa.getBody(), fb.getBody());
            }
        }

        // handle player collisions
        if(a == PLAYER){
            if(b == ENEMY){
                EnemyCollisionHandler.collide(fb.getBody(), fa.getBody());
            }else if(fb.getUserData().equals("goal")){
                screen.levelUp();
            }else if(b == PICKUP){
                ((Hero) fa.getBody().getUserData()).pickup((Pickup) fb.getBody().getUserData());
                ((Pickup) fb.getBody().getUserData()).setToDestroy();
            }
        }else if(b == PLAYER){
            if(a == ENEMY){
                EnemyCollisionHandler.collide(fa.getBody(), fb.getBody());
            }else if(fa.getUserData().equals("goal")){
                screen.levelUp();
            }else if(a == PICKUP){
                ((Hero) fb.getBody().getUserData()).pickup((Pickup) fa.getBody().getUserData());
                ((Pickup) fa.getBody().getUserData()).setToDestroy();
            }
        }

    }

    // called when two fixtures no longer collide
    public void endContact(Contact c) {}

    public void preSolve(Contact c, Manifold m) {}
    public void postSolve(Contact c, ContactImpulse ci) {}

    private static class ProjectileCollisionHandler{
        public static void collide(Projectile p, Body body) {
            // if it's an entity, damage it
            boolean friendly = p.getBody().getFixtureList().first().getFilterData().categoryBits == PLAYER_PROJECTILE;
            String userData = (String) body.getFixtureList().first().getUserData();

            if( body.getUserData() instanceof Hero){
                if (((Hero) body.getUserData()).hasPower(Pickup.Type.INVINCIBLE)) {
                    System.out.println("INVINCIBLE BOUNCE");
                    p.setBounces(1);
                    p.bounce();
                } else {
                    ((Hero) body.getUserData()).damage(p.getDamageAmount());
                }
            }else if (body.getUserData() instanceof Enemy){
                ((B2DSprite) body.getUserData()).damage(p.getDamageAmount());
            }else if( body.getUserData() instanceof Projectile){
                ((Projectile) body.getUserData()).setToDestroy();
            }

            // handle bouncing
            if( userData.equals("wall")){
                p.bounce();
            }else if(userData.equals("barrier") && !friendly){
                p.setBounces(1);
                p.bounce();
            }else{
                p.setToDestroy();
            }

        }
    }

    private static class EnemyCollisionHandler{
        public static void collide(Body enemyBody, Body body) {
            // if it's an entity, damage it
            if(body.getUserData() instanceof Hero){
                if(((Hero) body.getUserData()).hasPower(Pickup.Type.INVINCIBLE)){
                    ((Enemy) enemyBody.getUserData()).damage(9000);
                }else{
                    ((Hero) body.getUserData()).damage(((Enemy) enemyBody.getUserData()).getAttackDamage());
                    Vector2 v = enemyBody.getLinearVelocity();
                    v.scl(2.5f);
                    enemyBody.setLinearVelocity(new Vector2(-v.x, -v.y));
                    body.setLinearVelocity(new Vector2(v.x, v.y));
                }
            }else if( body.getUserData() instanceof Barrier){
                ((Enemy) enemyBody.getUserData()).stun(1.0f);
                Vector2 v = enemyBody.getLinearVelocity();
                v.scl(2.5f);
                enemyBody.setLinearVelocity(new Vector2(-v.x, -v.y));
            }
        }
    }

}
