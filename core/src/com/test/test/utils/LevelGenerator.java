package com.test.test.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
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

    private static TextureRegion[] floorTiles;
    private static TextureRegion floorTile;
    public LevelGenerator(GameScreen screen, Texture worldTexture){
        this.screen = screen;
        atlas = screen.getAtlas();
        System.out.println(worldTexture.toString());
        TextureRegion[][] splitTiles = TextureRegion.split(worldTexture, TILE_SIZE, TILE_SIZE);

        floorTile = splitTiles[1][1];
    }

    public Vector2 getRandomTile(){
        Room randomRoom = rooms.get(MathUtils.random(rooms.size - 1));
        return randomRoom.getRandomTile();
    }

    public TiledMap generateLevel(int width, int height, float seed){
        this.map = new TiledMap();
        this.mapWidth = width;
        this.mapHeight = height;
        terrainLayer = new TiledMapTileLayer(mapWidth, mapHeight, TILE_SIZE, TILE_SIZE);
        int numRooms = 9;
        placeRooms(numRooms);
        defineBox2d();
        map.getLayers().add(terrainLayer);
        return map;
    }

    private void defineBox2d(){
        for( int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++){
                TiledMapTileLayer.Cell cell = terrainLayer.getCell(x, y);
                if( cell == null ) continue;
                System.out.println(cell.toString());
                if (terrainLayer.getCell(x-1, y) == null) placeWall(x-1, y);
                if (terrainLayer.getCell(x+1, y) == null) placeWall(x+1, y);
                if (terrainLayer.getCell(x, y-1) == null) placeWall(x, y-1);
                if (terrainLayer.getCell(x, y+1) == null) placeWall(x, y+1);
            }
        }
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
        screen.getWorld().createBody(bdef).createFixture(fdef).setUserData("wall");

        /* SET WALL TEXTURE */
    }

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

    private void addToMap(Room room){
        for (int col = room.x1; col < room.x2; col++) {
            for (int row = room.y1; row < room.y2; row++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                TextureRegion tex = floorTile;
                cell.setTile(new StaticTiledMapTile(tex));
                terrainLayer.setCell(col, row, cell);
            }
        }
    }

    private void horizontalCorridor(int x1, int x2, int y){
        for(int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++){
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(new StaticTiledMapTile(floorTile));
            terrainLayer.setCell(x, y, cell);
        }
    }

    private void verticalCorridor(int y1, int y2, int x){
        for(int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++){
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(new StaticTiledMapTile(floorTile));
            terrainLayer.setCell(x, y, cell);
        }
    }


    private class Room{

        private int x1, x2, y1, y2;

        private int centerX;
        private int centerY;

        private Room(int x1, int y1, int width, int height){
            System.out.printf("Creating room with (%d, %d, %d, %d)\n", x1, y1, width, height);

            this.x1 = x1;
            this.x2 = x1 + width;
            this.y1 = y1;
            this.y2 = y1 + height;
            centerX = (x1 + x2) / 2;
            centerY = (y1 + y2) / 2;

            System.out.printf("Finishing room with (%d, %d, %d, %d)\n", x2, y2, centerX, centerY);
        }

        private boolean intersects(Room r){
            return (x1 <= r.x2 && x2 >= r.x1 &&
                    y1 <= r.y2 && r.y2 >= r.y1);
        }

        private Vector2 getRandomTile(){
            return new Vector2(MathUtils.random(x1, x2), MathUtils.random(y1, y2));
        }

    }
}
