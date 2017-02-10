package com.test.test.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.models.B2DSprite;
import com.test.test.models.Enemy;
import com.test.test.models.Hero;
import com.test.test.models.Projectile;
import com.test.test.screens.GameScreen;


/**
 * Created by Fionn on 21/11/2016.
 */
public class WorldContactListener implements ContactListener{

    public static final short PLAYER = 0x1;
    public static final short WALL = 0x2;
    public static final short ENEMY = 0x4;
    public static final short PLAYER_PROJECTILE = 0x8;
    public static final short ENEMY_PROJECTILE = 0x16;
    public static final short BARRIER = 0x32;
    public static final short PICKUP = 0x64;

    private GameScreen screen;

    public WorldContactListener(GameScreen screen){
        this.screen = screen;
    }

    public void beginContact(Contact c){
        Fixture fa = c.getFixtureA();
        Fixture fb = c.getFixtureB();

        short a = fa.getFilterData().categoryBits;
        short b = fb.getFilterData().categoryBits;

        if(fa.getUserData() != null && fb.getUserData() != null){
//            if(fa.getUserData().equals("player") && fb.getUserData().equals("enemy") ) {
//                Vector2 v = fa.getBody().getLinearVelocity();
//                fa.getBody().setLinearVelocity(new Vector2(-v.x, -v.y));
//                fb.getBody().setLinearVelocity(new Vector2(v.x, v.y));
//                ((Hero) fa.getBody().getUserData()).damage(3);
//            }else if(fa.getUserData().equals("enemy") && fb.getUserData().equals("player")){
//                Vector2 v = fb.getBody().getLinearVelocity();
//                fb.getBody().setLinearVelocity(new Vector2(-v.x, -v.y));
//                fa.getBody().setLinearVelocity(new Vector2(v.x, v.y));
//                ((Hero) fb.getBody().getUserData()).damage(3);
//            }else if(fa.getUserData().equals("enemy") && fb.getUserData().equals("player-fireball")) {
//                ((Enemy) fa.getBody().getUserData()).damage(((Projectile) fb.getBody().getUserData()).damageAmount);
//                ((Projectile) fb.getBody().getUserData()).setToDestroy();
//            }else if(fa.getUserData().equals("player-fireball") && fb.getUserData().equals("enemy")) {
//                ((Projectile) fa.getBody().getUserData()).setToDestroy();
//                ((Enemy) fb.getBody().getUserData()).setToDestroy();
//            }else if(fa.getBody().getUserData() instanceof Projectile &&
//                    (fb.getUserData().equals("wall") || fb.getUserData().equals("barrier")) ) {
//                ((Projectile) fa.getBody().getUserData()).setToDestroy();
//            }else if((fa.getUserData().equals("wall") || fa.getUserData().equals("barrier")) && fb.getBody().getUserData() instanceof Projectile) {
//                ((Projectile) fb.getBody().getUserData()).setToDestroy();
//            }else if(fa.getUserData().equals("barrier") && fb.getUserData().equals("enemy")){
//                Vector2 v = fb.getBody().getLinearVelocity();
//                fb.getBody().setLinearVelocity(new Vector2(v.x * -2.5f, v.y * -2.5f));
//                ((Enemy) fb.getBody().getUserData()).stun();
//            }else if(fb.getUserData().equals("barrier") && fa.getUserData().equals("enemy")){
//                Vector2 v = fa.getBody().getLinearVelocity();
//                fa.getBody().setLinearVelocity(new Vector2(v.x * -2.5f, v.y * -2.5f));
//                ((Enemy) fa.getBody().getUserData()).stun();
//            }else if((fa.getUserData().equals("goal") && fb.getUserData().equals("player"))
//                    || (fa.getUserData().equals("player") && fb.getUserData().equals("goal"))){
//                screen.levelUp();
//            }else{
//
//            }

            // handle projectile collisions
            if(a == PLAYER_PROJECTILE || a == ENEMY_PROJECTILE){
                if((b == ENEMY || b == PLAYER)){
                    ((B2DSprite) fb.getBody().getUserData()).damage(((Projectile) fa.getBody().getUserData()).getDamageAmount());
                }
                ((Projectile) fa.getBody().getUserData()).setToDestroy();
            }else if(b == PLAYER_PROJECTILE || b == ENEMY_PROJECTILE){
                if (a == ENEMY || a == PLAYER){
                    System.out.println(a);
                    System.out.println(fa.getUserData());
                    System.out.println(fb.getUserData());
                    ((B2DSprite) fa.getBody().getUserData()).damage(((Projectile) fb.getBody().getUserData()).getDamageAmount());
                }else{
                    System.out.println(" .. " + a);

                }
                ((Projectile) fb.getBody().getUserData()).setToDestroy();
            }

            if(a == PLAYER){
                if(b == ENEMY){
                    ((Hero) fa.getBody().getUserData()).damage(((Enemy) fb.getBody().getUserData()).getAttackDamage());
                    Vector2 v = fa.getBody().getLinearVelocity();
                    fa.getBody().setLinearVelocity(new Vector2(-v.x, -v.y));
                    fb.getBody().setLinearVelocity(new Vector2(v.x, v.y));

                }else if(fb.getUserData().equals("goal")){
                    screen.levelUp();
                }
            }


        }

    }

    // called when two fixtures no longer collide
    public void endContact(Contact c) {}

    public void preSolve(Contact c, Manifold m) {}
    public void postSolve(Contact c, ContactImpulse ci) {}

    public void collide(B2DSprite a, B2DSprite b){
        if( a instanceof Hero ){
            Hero hero = (Hero) a;
            a.damage( ((Enemy) b).getAttackDamage() );
        }
    }
}
