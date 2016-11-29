package com.test.test.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.test.test.models.B2DSprite;
import com.test.test.screens.GameScreen;

import static com.test.test.SpaceAnts.PPM;

/**
 * Created by fionn on 22/11/16.
 */
public class LevelGenerator {

    private static final int TILE_SIZE = 20;
    private static final int MAX_ROOM_SIZE = 12;
    private static final int MIN_ROOM_SIZE = 4;

    private int mapWidth;
    private int mapHeight;

    private TiledMap map;
    private TiledMapTileLayer terrainLayer;
    private TextureAtlas atlas;
    private Array<Room> rooms;
    private GameScreen screen;
    private B2DSprite goal;

    private Array<Body> wallBodies;

    private int numRooms;

    private static Array<TextureRegion> floorTiles;

    public LevelGenerator(GameScreen screen, Texture worldTexture){
        this.screen = screen;
        this.numRooms = 4;
        this.wallBodies = new Array<Body>();
        atlas = screen.getAtlas();
        TextureRegion[][] splitTiles = TextureRegion.split(worldTexture, TILE_SIZE, TILE_SIZE);
        floorTiles = new Array<TextureRegion>();

        for(int x=1; x <= 2; x++){
            for( int y = 1; y <= 2; y++){
                floorTiles.add(splitTiles[x][y]);
            }
        }
    }

    public void destroyLevel(){
        System.out.println("DESTROY");
        System.out.println(screen.getWorld().getBodyCount());
        System.out.printf("%d walls \n", wallBodies.size);
        screen.getWorld().destroyBody(goal.getBody());
        for(Body b : wallBodies){
            System.out.print("a");
            screen.getWorld().destroyBody(b);
        }
    }

    public TiledMap generateLevel(int width, int height, float seed){
        numRooms = (Math.round(seed * 100) % 10) + 1;
        System.out.printf("Generating level with a seed of %f  [%d rooms]  \n", seed, numRooms);

        this.map = new TiledMap();
        this.mapWidth = width;
        this.mapHeight = height;
        terrainLayer = new TiledMapTileLayer(mapWidth, mapHeight, TILE_SIZE, TILE_SIZE);
        placeRooms(numRooms);
        createGoal();
        defineBox2d();
        map.getLayers().add(terrainLayer);
        return map;
    }

    public Vector2 getHeroStart(){ return rooms.get(0).getRandomTile(); }
    public Vector2 getGoalTile(){
        return rooms.get(rooms.size - 1).getRandomTile();
    }
    public Vector2 getRandomTile(){ return rooms.get(MathUtils.random(1, rooms.size - 1)).getRandomTile(); }


    private void placeRooms(int n){
        rooms = new Array<Room>();
        boolean roomIntersects;

        for(int i = 0; i < n; i++){
            int w = MathUtils.random(MIN_ROOM_SIZE, MAX_ROOM_SIZE);
            int h = MathUtils.random(MIN_ROOM_SIZE, MAX_ROOM_SIZE);
            int x = MathUtils.random(mapWidth - w);
            int y = MathUtils.random(mapHeight - h);

            Room newRoom = new Room(x, y, w, h);

            roomIntersects = false;

            // check if the room intersects an already placed room
            for( Room r : rooms ){
                if( newRoom.intersects(r) ){
                    // if so, try again
                    roomIntersects = true;
                    i--;
                    System.out.println("intersect");
                    break;
                }
            }
            // if not, add it to the level
            if(!roomIntersects){
                // add corridors between rooms
                if( rooms.size != 0 ){
                    int prevX = rooms.get(rooms.size - 1).centerX;
                    int prevY = rooms.get(rooms.size - 1).centerY;

                    if(MathUtils.random(1) == 1){
                        horizontalCorridor(prevX, newRoom.centerX, prevY);
                        verticalCorridor(prevY, newRoom.centerY, newRoom.centerX);
                    }else{
                        verticalCorridor(prevY, newRoom.centerY, prevX);
                        horizontalCorridor(prevX, newRoom.centerX, newRoom.centerY);
                    }
                }
                addToMap(newRoom);
                rooms.add(newRoom);
            }
        }

    }

    private void defineBox2d(){
        for( int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++){
                TiledMapTileLayer.Cell cell = terrainLayer.getCell(x, y);
                if( cell == null ) continue;
                if (terrainLayer.getCell(x-1, y) == null) placeWall(x-1, y);
                if (terrainLayer.getCell(x+1, y) == null) placeWall(x+1, y);
                if (terrainLayer.getCell(x, y-1) == null) placeWall(x, y-1);
                if (terrainLayer.getCell(x, y+1) == null) placeWall(x, y+1);
            }
        }
    }

    private void createGoal(){
        BodyDef bdef = new BodyDef();
        Vector2 goalPosition = getGoalTile();
        bdef.position.set((goalPosition.x + 0.5f) * 20 / PPM, goalPosition.y * 20 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.linearDamping = 5.0f;
        bdef.fixedRotation = true;

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(5 / PPM);
        fdef.shape = shape;
        fdef.friction = 0.75f;
        fdef.restitution = 0.0f;
        fdef.isSensor = true;

        Body b2body = screen.getWorld().createBody(bdef);
        b2body.createFixture(fdef).setUserData("goal");
        this.goal = new B2DSprite(b2body);
        b2body.setUserData(goal);
    }


    private void placeWall(int x, int y){
        Vector2 centre = new Vector2((x + 0.5f) * TILE_SIZE / 2 / PPM,
                                     (y + 0.5f) * TILE_SIZE / 2 / PPM);
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(centre);
        PolygonShape wall = new PolygonShape();
        wall.setAsBox(TILE_SIZE / 2 / PPM, TILE_SIZE / 2 / PPM, centre, 0);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = wall;

        Body body = screen.getWorld().createBody(bdef);
        body.createFixture(fdef).setUserData("wall");
        wallBodies.add(body);
        /* SET WALL TEXTURE */
    }

    private void addToMap(Room room){
        for (int col = room.x1; col < room.x2; col++) {
            for (int row = room.y1; row < room.y2; row++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(randomFloorTile()));
                terrainLayer.setCell(col, row, cell);
            }
        }
    }

    private void horizontalCorridor(int x1, int x2, int y){
        for(int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++){
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(new StaticTiledMapTile(randomFloorTile()));
            terrainLayer.setCell(x, y, cell);
        }
    }

    private void verticalCorridor(int y1, int y2, int x){
        for(int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++){
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(new StaticTiledMapTile(randomFloorTile()));
            terrainLayer.setCell(x, y, cell);
        }
    }

    private TextureRegion randomFloorTile(){
        return floorTiles.get(MathUtils.random(0, floorTiles.size - 1));
    }

    private class Room{

        private int x1, x2, y1, y2;

        private int centerX;
        private int centerY;

        private Room(int x1, int y1, int width, int height){
            this.x1 = x1;
            this.x2 = x1 + width;
            this.y1 = y1;
            this.y2 = y1 + height;
            centerX = (x1 + x2) / 2;
            centerY = (y1 + y2) / 2;
        }

        private boolean intersects(Room r){
            return (x1 <= r.x2 && x2 >= r.x1 &&
                    y1 <= r.y2 && r.y2 >= r.y1);
        }

        private Vector2 getRandomTile(){
            return new Vector2(MathUtils.random(x1, x2 - 1), MathUtils.random(y1, y2 - 1));
        }

    }
}
