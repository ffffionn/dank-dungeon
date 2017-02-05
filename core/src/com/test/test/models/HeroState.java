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

        if(Gdx.input.justTouched()){
            if( hero.getCurrentState() != blocking){
                hero.shoot();
            }
        }

        Vector2 heroVelocity = hero.getBody().getLinearVelocity();

        if (Gdx.input.isKeyPressed(Input.Keys.W) && heroVelocity.y < Hero.MAX_VELOCITY) {
            hero.getBody().applyLinearImpulse(new Vector2(0, 0.2f), hero.getBody().getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && heroVelocity.y > -Hero.MAX_VELOCITY) {
            hero.getBody().applyLinearImpulse(new Vector2(0, -0.2f), hero.getBody().getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && heroVelocity.x < Hero.MAX_VELOCITY) {
            hero.getBody().applyLinearImpulse(new Vector2(0.2f, 0), hero.getBody().getWorldCenter(), true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && heroVelocity.x > -Hero.MAX_VELOCITY) {
            hero.getBody().applyLinearImpulse(new Vector2(-0.2f, 0), hero.getBody().getWorldCenter(), true);
        }

    }


    /**
     * Define the hero's animations given a TextureAtlas
     * @param atlas The atlas containing the various animations.
     */
    public static void defineAnimations(TextureAtlas atlas){
        TextureAtlas.AtlasRegion region;

        region = atlas.findRegion("player-move");
        moveAnimation = region.split(64, 64)[0];

        region = atlas.findRegion("player-cast");
        castAnimation = region.split(64, 64)[0];

        region = atlas.findRegion("player-die");
        dieAnimation = region.split(64, 64)[0];

        region = atlas.findRegion("player-strafe");
        standAnimation = region.split(64, 64)[0];

        region = atlas.findRegion("player-cast-onehand");
        blockAnimation = region.split(64, 64)[0];

        region = atlas.findRegion("player-wobble");
        runAnimation = region.split(64, 64)[0];

        region = atlas.findRegion("player-cast-forward");
        castAnimation2 = region.split(64, 64)[0];

    }

}
