package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * A more specialised set of states for the player character.
 */
public abstract class HeroState{

    public static DeadState dead = new DeadState();
    public static BlockingState blocking = new BlockingState();
    public static StandingState standing = new StandingState();
    public static SprintingState sprinting = new SprintingState();
    public static AttackingState attacking = new AttackingState();

    protected static TextureRegion[] moveAnimation;
    protected static TextureRegion[] standAnimation;
    protected static TextureRegion[] castAnimation;
    protected static TextureRegion[] dieAnimation;
    protected static TextureRegion[] blockAnimation;
    protected static TextureRegion[] runAnimation;
    protected static TextureRegion[] castAnimation2;

    public void enter(Hero hero){}
    public void leave(Hero hero) {}
    public void update(float dt, Hero hero) {}

    protected float getRunModifier(){ return 1.0f;}

    protected void handleMovement(Hero hero){
        Vector2 heroVelocity = hero.getBody().getLinearVelocity();
        Vector2 movement = new Vector2(0, 0);
        float modifier = getRunModifier();
        if (Gdx.input.isKeyPressed(Input.Keys.W) && heroVelocity.y < Hero.MAX_VELOCITY) {
            movement.y += 0.2f * modifier;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && heroVelocity.y > -Hero.MAX_VELOCITY) {
            movement.y -= 0.2f * modifier;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && heroVelocity.x < Hero.MAX_VELOCITY) {
            movement.x += 0.2f * modifier;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && heroVelocity.x > -Hero.MAX_VELOCITY) {
            movement.x -= 0.2f * modifier;
        }
        hero.getBody().applyLinearImpulse(movement, hero.getBody().getWorldCenter(), true);
    }

    /**
     * Define the hero's animations given a TextureAtlas
     * @param atlas The atlas containing the various animations.
     */
    public static void defineAnimations(TextureAtlas atlas){
        standAnimation = atlas.findRegion("player-strafe").split(64, 64)[0];
        moveAnimation = atlas.findRegion("player-move").split(64, 64)[0];
        runAnimation = atlas.findRegion("player-wobble").split(64, 64)[0];
        blockAnimation = atlas.findRegion("player-cast-forward").split(64, 64)[0];
        castAnimation = atlas.findRegion("player-cast").split(64, 64)[0];
        castAnimation2 = atlas.findRegion("player-cast-onehand").split(64, 64)[0];
        dieAnimation = atlas.findRegion("player-die").split(64, 64)[0];
    }

//
//    public static class AttackingState extends HeroState {
//
//        private float timer;
//
//        @Override
//        public void update(float dt, Hero hero) {
//            // if space released or mana empty, return to previous state
//            handleMovement(hero);
//            timer += dt;
//            if(timer > 0.15f){
//                hero.changeState(hero.getPreviousState());
//            }
//            System.out.println("?!");
//        }
//
//        @Override
//        public void enter(Hero hero){
//            timer = 0.0f;
//            hero.setAnimation(castAnimation, 1 / 32f);
//        }
//
//        public String toString(){ return "attacking"; }
//
//
//    }

}
