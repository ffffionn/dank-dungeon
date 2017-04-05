package com.test.test.models;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.screens.GameScreen;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.models.Hero.HERO_SIZE;
import static com.test.test.utils.WorldContactListener.*;

/**
 * The Hero's shield. Repels enemies and drains mana while up.
 */
public class Barrier extends AnimatedB2DSprite{

    private GameScreen screen;
    private Hero hero;
    private float shieldSize;
    private boolean raised;

    public Barrier(GameScreen screen, Hero hero){
        super();
        this.screen = screen;
        this.hero = hero;
        this.shieldSize = 10.5f;
        setToDestroy = false;
        raised = false;
        defineShield(hero.getPosition());
        b2body.setActive(false);
        TextureRegion[][] split = screen.getAtlas().findRegion("shield").split(48, 48);
        TextureRegion[] frames = new TextureRegion[16];
        int index = 0;
        for(int x = 0; x < split.length; x++){
            for(int y = 0; y < split[0].length; y++){
                frames[index++] = split[x][y];
            }
        }
        setAnimation(frames, 1/12f);
        setTexture(frames[0], 24);
    }

    public void update(float dt) {
        b2body.setTransform(hero.getPosition(), hero.angleToCursor());
        super.update(dt);
        if(raised){
            hero.adjustMana(-5 * dt);
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        if(raised){
            sprite.setRegion(animation.getFrame());
            sprite.rotate90(true);
            sprite.draw(sb);
        }
    }

    public void raise(){
        b2body.setActive(true);
        raised = true;
    }

    public void lower(){
        b2body.setActive(false);
        raised = false;
    }

    private void defineShield(Vector2 position) {
        BodyDef bdef = new BodyDef();
        position.x += HERO_SIZE / 3 / PPM;
        bdef.position.set(position.x, position.y);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        b2body = screen.getWorld().createBody(bdef);

        CircleShape shape = new CircleShape();
        shape.setRadius(HERO_SIZE / 2 /PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.friction = 0.0f;
        fdef.restitution = 1.0f;
        fdef.filter.categoryBits = BARRIER;
        fdef.filter.maskBits = PLAYER_PROJECTILE | ENEMY_PROJECTILE | ENEMY;

        b2body.createFixture(fdef).setUserData("barrier");

        b2body.setUserData(this);
        shape.dispose();
    }

    @Override
    public void setToDestroy() {
        super.setToDestroy();
        raised = false;
    }
}
