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

    public Hero defineHero(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(60 / PPM, 60 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 5.0f;
        bdef.fixedRotation = true;
        Body b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(7 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

        b2body.createFixture(fdef);

        Hero player = new Hero(b2body, world, screen);

        return player;
    }

    public B2DSprite defineEnemy(){
        BodyDef bdef = new BodyDef();
        bdef.position.set(60 / PPM, 60 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 5.0f;
        bdef.fixedRotation = true;
        Body b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(7 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

        b2body.createFixture(fdef);

        return new B2DSprite(b2body);
    }


    public Body defineCursor(){
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(32 / PPM, 32 / PPM);

        CircleShape shape = new CircleShape();
        shape.setRadius(5 / PPM);

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

    public void defineMap(Texture tiles) {
        //        map = LevelGenerator.NewLevel()

        int tileSize = 20;
        TextureRegion[][] splitTiles = TextureRegion.split(tiles, tileSize, tileSize);

        TextureRegion centreFloor = splitTiles[1][1];
        MapLayers layers = screen.getMap().getLayers();

        TiledMapTileLayer layer = new TiledMapTileLayer(16, 16, tileSize, tileSize);
        for (int col = 0; col < layer.getWidth(); col++) {
            for (int row = 0; row < layer.getHeight(); row++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                TextureRegion tex = centreFloor;
                if(col == 0 || row == 0 || col == 15 || row == 15){
                    Vector2 centre = new Vector2((col + 0.5f) * tileSize / 2 / PPM,
                                                 (row + 0.5f) * tileSize / 2 / PPM);
                    BodyDef bdef = new BodyDef();
                    bdef.type = BodyDef.BodyType.StaticBody;
                    bdef.position.set(centre);

                    PolygonShape wall = new PolygonShape();
                    wall.setAsBox(tileSize / 2 / PPM, tileSize / 2 / PPM, centre, 0);
                    FixtureDef fdef = new FixtureDef();
                    fdef.shape = wall;

                    world.createBody(bdef).createFixture(fdef);
                    tex = splitTiles[0][8];
                }
                cell.setTile(new StaticTiledMapTile(tex));
                layer.setCell(row, col, cell);
            }
        }

        layers.add(layer);
    }


}
