package com.test.test.models;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.test.test.screens.GameScreen;

import static com.test.test.utils.CaveGenerator.worldPositionToCell;

/**
 * Wolf Enemy type. Slow and tanky enemy, deals high damage.
 */
public class Wolf extends Enemy {

    private boolean[][] floor;

    public Wolf(GameScreen screen, Vector2 startPosition){
        super(screen, startPosition);
        this.floor = screen.getLevelMap();

        // Wolf attributes
        this.health = 500;
        this.max_speed = 0.35f;
        this.score_value = 200;
        this.attackDamage = 9;
        this.radius = 9.0f;
        this.sightRadius = 1.8f;

        define(startPosition);
    }

    public Wolf(GameScreen screen, Vector2 startPosition, float speed, int hp){
        this(screen, startPosition);
        this.max_speed = speed;
        this.health = hp;
    }

    @Override
    protected void move() {
        if(targetInSight()){
            moveTowards(target);
        }else{
            moveRandom();
        }
    }

    private void moveRandom(){
        Vector2 pos = worldPositionToCell(b2body.getPosition().nor());
        int x = Math.round(pos.x);
        int y = Math.round(pos.y);
        Vector2 dir = new Vector2(0,0);
//        dir = b2body.getPosition().cpy();

        float angle = b2body.getAngle();


        System.out.printf("B: %s \t P: %s  \n", b2body.getPosition(), pos);
        if(y >= floor[0].length || floor[x][y + 1]){
            dir = dir.add(0, -1);
        }else if(y == 0 || floor[x][y - 1]){
            dir = dir.add(0, 1);
        }else if(x == 0 || floor[x - 1][y]){
            dir = dir.add(1, 0);
        }else if(x >= floor.length || floor[x + 1][y]){
            dir = dir.add(-1, 0);
        }else{
//            dir.x += 1.0f;
//            dir.add(MathUtils.random(-1, 1), MathUtils.random(-1, 1));
            dir.add(MathUtils.randomTriangular(1.0f), MathUtils.randomTriangular(1.0f));
        }
        System.out.println(dir.scl(0.5f));
        System.out.println(dir);

        if( b2body.getLinearVelocity().x < max_speed && b2body.getLinearVelocity().y < max_speed){
            b2body.applyLinearImpulse(dir.nor().scl(max_speed), b2body.getWorldCenter(), true);
        }
//        moveTowards(dir.add(b2body.getPosition()));

//        moveTowards(dir);

    }
}
