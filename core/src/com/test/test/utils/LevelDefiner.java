package com.test.test.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.test.test.models.B2DSprite;
import com.test.test.models.Enemy;
import com.test.test.models.Fireball;
import com.test.test.models.Hero;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;


/**
 * Created by Fionn on 20/11/2016.
 */
public class LevelDefiner {

    private GameScreen screen;
    private World world;

    public LevelDefiner(World world, GameScreen screen){
        this.world = world;
        this.screen = screen;
    }

    public Enemy defineEnemy(int x, int y){
        BodyDef bdef = new BodyDef();
        bdef.position.set((x + 0.5f) * 20 / PPM, y * 20 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 5.0f;
        bdef.fixedRotation = true;
        Body b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(5 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

        b2body.createFixture(fdef).setUserData("enemy");

        Enemy e = new Enemy(b2body, screen);
        b2body.setUserData(e);
        return e;
    }


    public Body defineCursor(){
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(0, 0);

        CircleShape shape = new CircleShape();
        shape.setRadius(3 / PPM);

        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 0f;
        fdef.friction = 0f;
        fdef.isSensor = true;
        fdef.restitution = 0f;

        Body cursorBody = world.createBody(bdef);
        cursorBody.createFixture(fdef);
        return cursorBody;
    }

}
