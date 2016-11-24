package com.test.test.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
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
    private static final int MAX_ROOM_SIZE = 24;
    private static final int MIN_ROOM_SIZE = 6;

    private int mapWidth;
    private int mapHeight;

    private TiledMap map;
    private TiledMapTileLayer terrainLayer;
    private TextureAtlas atlas;

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



    public TiledMap generateLevel(int width, int height, float seed){
        this.map = new TiledMap();
        this.mapWidth = width;
        this.mapHeight = height;
        terrainLayer = new TiledMapTileLayer(mapWidth, mapHeight, TILE_SIZE, TILE_SIZE);
        int numRooms = 5;
        placeRooms(numRooms);
        defineBox2d();
        map.getLayers().add(terrainLayer);
        return map;
    }

    private void defineBox2d(){

    }

    private void placeRooms(int n){
        Array<Room> rooms = new Array<Room>();
        boolean roomIntersects;

        for(int i = 0; i < n; i++){
            int w = MathUtils.random(MIN_ROOM_SIZE + 1, MAX_ROOM_SIZE);
            int h = MathUtils.random(MIN_ROOM_SIZE + 1, MAX_ROOM_SIZE);
            int x = MathUtils.random(mapWidth - w + 1);
            int y = MathUtils.random(mapHeight - h + 1);

            Room newRoom = new Room(x, y, w, h);

            roomIntersects = false;

            // check if the room intersects an already placed room
            for( Room r : rooms ){
                if( newRoom.intersects(r) ){
                    // if so, try again
                    i--;
                    roomIntersects = true;
                    System.out.println("intersect");
                }
            }
            // if not, add it to the level
            if(!roomIntersects){
                addToMap(newRoom);
                rooms.add(newRoom);
                // add corridors between rooms
                if( rooms.size != 0 ){
                    int prevX = rooms.get(rooms.size - 1).centerX;
                    int prevY = rooms.get(rooms.size - 1).centerY;

            /* DO RANDOM VARIATION */
                    horizontalCorridor(prevX, newRoom.centerX, prevY);
                    verticalCorridor(prevY, newRoom.centerY, newRoom.centerX);
                }
            }
        }

    }

    private void addToMap(Room room){
        for (int col = room.x1; col < room.x2; col++) {
            for (int row = room.y1; row < room.y2; row++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                TextureRegion tex = floorTile;
                if(col == room.x1 || row == room.y1 || col == room.x2 || row == room.y2){
                    Vector2 centre = new Vector2((col + 0.5f) * TILE_SIZE / 2 / PPM,
                                                 (row + 0.5f) * TILE_SIZE / 2 / PPM);
                    BodyDef bdef = new BodyDef();
                    bdef.type = BodyDef.BodyType.StaticBody;
                    bdef.position.set(centre);

                    PolygonShape wall = new PolygonShape();
                    wall.setAsBox(TILE_SIZE / 2 / PPM, TILE_SIZE / 2 / PPM, centre, 0);
                    FixtureDef fdef = new FixtureDef();
                    fdef.shape = wall;
                    screen.getWorld().createBody(bdef).createFixture(fdef).setUserData("wall");
                    tex = floorTile;
                }
                cell.setTile(new StaticTiledMapTile(tex));
//                System.out.printf("placing tile at (%d, %d) \n", row, col);
                terrainLayer.setCell(row, col, cell);
            }
        }

    }

    private void horizontalCorridor(int x1, int x2, int y){
        for(int x = Math.min(x1, x2); x < Math.max(x1, x2); x++){
            // tile size/world size  -> x,y ?  get precise nigga
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            // make walls around corridor?
            cell.setTile(new StaticTiledMapTile(floorTile));
            terrainLayer.setCell(x, y, cell);
        }
    }

    private void verticalCorridor(int y1, int y2, int x){
        for(int y = Math.min(y1, y2); y < Math.max(y1, y2); y++){
            // tile size/world size  -> x,y ?  get precise nigga
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            // make walls around corridor?
            cell.setTile(new StaticTiledMapTile(floorTile));
            terrainLayer.setCell(x, y, cell);
        }
    }


    private class Room{

        private int x1, x2, y1, y2;

        private int centerX;
        private int centerY;

        public Room(int x1, int y1, int width, int height){
            System.out.printf("Creating room with (%d, %d, %d, %d)\n", x1, y1, width, height);

            this.x1 = x1;
            this.x2 = x1 + width;
            this.y1 = y1;
            this.y2 = y1 + height;
            centerX = (x1 + x2) / 2;
            centerY = (y1 + y2) / 2;

            System.out.printf("Finishing room with (%d, %d, %d, %d)\n", x2, y2, centerX, centerY);
        }

        public boolean intersects(Room r){
            return (x1 <= r.x2 && x2 >= r.x1 &&
                    y1 <= r.y2 && r.y2 >= r.y1);
        }
    }
}
