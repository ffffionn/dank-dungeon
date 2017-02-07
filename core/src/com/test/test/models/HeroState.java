package com.test.test.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Fionn on 04/02/2017.
 */
public class HeroState {

    public static BlockingState blocking = new BlockingState();
    public static StandingState standing = new StandingState();
    public static SprintingState sprinting = new SprintingState();
    public static DeadState dead = new DeadState();

    protected static TextureRegion[] moveAnimation;
    protected static TextureRegion[] standAnimation;
    protected static TextureRegion[] castAnimation;
    protected static TextureRegion[] dieAnimation;
    protected static TextureRegion[] blockAnimation;
    protected static TextureRegion[] runAnimation;
    protected static TextureRegion[] castAnimation2;

    public void enter(Hero hero){}
    public void update(float dt){}

    public void handleInput(Hero hero){
        // default alive hero controls

        if(Gdx.input.isKeyPressed(Input.Keys.SPACE) && hero.getCurrentState() != blocking){
                hero.block();
        }

        if(Gdx.input.justTouched() && hero.getCurrentState() != blocking){
            hero.shoot();
        }

        Vector2 heroVelocity = hero.getBody().getLinearVelocity();
        Vector2 movement = new Vector2(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.W) && heroVelocity.y < Hero.MAX_VELOCITY) {
            movement.y += 0.2f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && heroVelocity.y > -Hero.MAX_VELOCITY) {
            movement.y -= 0.2f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && heroVelocity.x < Hero.MAX_VELOCITY) {
            movement.x += 0.2f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && heroVelocity.x > -Hero.MAX_VELOCITY) {
            movement.x -= 0.2f;
        }

        hero.getBody().applyLinearImpulse(movement, hero.getBody().getWorldCenter(), true);

    }


    /**
     * Define the hero's animations given a TextureAtlas
     * @param atlas The atlas containing the various animations.
     */
    public static void defineAnimations(TextureAtlas atlas){
        moveAnimation = atlas.findRegion("player-move").split(64, 64)[0];
        castAnimation = atlas.findRegion("player-cast").split(64, 64)[0];
        dieAnimation = atlas.findRegion("player-die").split(64, 64)[0];
        standAnimation = atlas.findRegion("player-strafe").split(64, 64)[0];
        blockAnimation = atlas.findRegion("player-cast-onehand").split(64, 64)[0];
        runAnimation = atlas.findRegion("player-wobble").split(64, 64)[0];
        castAnimation2 = atlas.findRegion("player-cast-forward").split(64, 64)[0];
    }

}
