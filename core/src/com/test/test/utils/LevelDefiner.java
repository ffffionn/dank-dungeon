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

    public Hero defineHero(int x, int y){
        BodyDef bdef = new BodyDef();
        System.out.printf("X: %d  Y: %d  \n", x, y);
        bdef.position.set(x * 20/ PPM, y * 20 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 10.0f;
        bdef.fixedRotation = true;
        Body b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(7 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;

        b2body.createFixture(fdef).setUserData("player");

        Hero player = new Hero(b2body, screen);

        return player;
    }

    public Enemy defineEnemy(int x, int y){
        BodyDef bdef = new BodyDef();
        bdef.position.set(x * 20 / PPM, y * 20 / PPM);
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
        layers.add(layer);

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
                    world.createBody(bdef).createFixture(fdef).setUserData("wall");
                    tex = splitTiles[0][8];
                }
                cell.setTile(new StaticTiledMapTile(tex));
                layer.setCell(row, col, cell);
            }
        }

        layers.add(layer);
    }


}
