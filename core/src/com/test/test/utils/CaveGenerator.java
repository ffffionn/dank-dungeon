package com.test.test.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.test.test.models.B2DSprite;
import com.test.test.models.Enemy;
import com.test.test.screens.GameScreen;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.test.test.SpaceAnts.PPM;

/**
 * Created by fionn on 07/01/17.
 */
public class CaveGenerator {


    private static final int MAX_ROOM_SIZE = 12;
    private static final int MIN_ROOM_SIZE = 4;
    private static final int TILE_SIZE = 20;

    private int mapWidth;
    private int mapHeight;
    private int numRooms;

    private TiledMapTileLayer terrainLayer;
    private GameScreen screen;
    private B2DSprite goal;
    private TiledMap map;

    private Array<TextureRegion> floorTiles;
    private Array<Body> wallBodies;

    private int birthLimit;
    private int deathLimit;
    private int numberOfSteps;

    private boolean[][] cellmap;

    public CaveGenerator(GameScreen screen, Texture worldTileSheet){
        this.screen = screen;
        this.numRooms = 4;
        this.wallBodies = new Array<Body>();
        this.map = new TiledMap();
        floorTiles = new Array<TextureRegion>();

        this.birthLimit = 5;
        this.deathLimit = 3;
        this.numberOfSteps = 4;

        TextureRegion[][] splitTiles = TextureRegion.split(worldTileSheet, TILE_SIZE, TILE_SIZE);
        for(int x=1; x <= 2; x++){
            for( int y = 1; y <= 2; y++){
                floorTiles.add(splitTiles[x][y]);
            }
        }
    }

    public TiledMap generateMap(int width, int height, float seed){
        //Create a new map
        mapHeight = height;
        mapWidth = width;
        this.cellmap = new boolean[mapWidth][mapHeight];
        //Set up the map with random values
        cellmap = initialiseMap(cellmap);
        //And now run the simulation for a set number of steps
        for(int i=0; i< numberOfSteps; i++){
            cellmap = doSimulationStep(cellmap);
        }

        defineLevel();
        createGoal();
        map.getLayers().add(terrainLayer);
        return map;
    }

    public void destroyLevel(){
        System.out.printf("Removing %d walls \n", wallBodies.size);
        for(Body b : wallBodies){
            screen.getWorld().destroyBody(b);
        }
        wallBodies.clear();
        screen.getWorld().destroyBody(goal.getBody());
        screen.getMap().getLayers().remove(terrainLayer);
        System.out.printf("-- %d bodies left after level tear down --\n", screen.getWorld().getBodyCount());
        for(int x=0; x < mapWidth; x++){
            for(int y=0; y < mapHeight; y++){
                cellmap[x][y] = false;
            }
        }
    }

    public boolean[][] initialiseMap(boolean[][] map){
        float chanceToStartAlive = 0.45f;

        for(int x=0; x < mapWidth; x++){
            for(int y=0; y < mapHeight; y++){
                if(random() < chanceToStartAlive){
                    map[x][y] = true;
                }
            }
        }
        return map;
    }

    public boolean[][] doSimulationStep(boolean[][] oldMap){
        boolean[][] newMap = new boolean[mapWidth][mapHeight];
        //Loop over each row and column of the map
        for(int x=0; x< oldMap.length; x++){
            for(int y=0; y< oldMap[x].length; y++){
                int nbs = this.countAliveNeighbours(oldMap, x, y);
                if(oldMap[x][y]){
                    if(nbs < deathLimit){
                        newMap[x][y] = false;
n                    else{
                        newMap[x][y] = true;
                    }
                } //Otherwise, if the cell is dead now, check if it has the right number of neighbours to be 'born'
                else{
                    if(nbs > birthLimit){
                        newMap[x][y] = true;
                    }
                    else{
                        newMap[x][y] = false;
                    }
                }
            }
        }
        return newMap;
    }

    public int countAliveNeighbours(boolean[][] map, int x, int y){
        int count = 0;
        for(int i=-1; i<2; i++){
            for(int j=-1; j<2; j++){
                int neighbour_x = x+i;
                int neighbour_y = y+j;
                //If we're looking at the middle point
                if(i == 0 && j == 0){
                    //Do nothing, we don't want to add ourselves in!
                }
                //In case the index we're looking at it off the edge of the map
                else if(neighbour_x < 0 || neighbour_y < 0 || neighbour_x >= map.length || neighbour_y >= map[0].length){
                    count = count + 1;
                }
                //Otherwise, a normal check of the neighbour
                else if(map[neighbour_x][neighbour_y]){
                    count = count + 1;
                }
            }
        }
        return count;
    }

    public void defineLevel(){
        terrainLayer = new TiledMapTileLayer(mapWidth, mapHeight, TILE_SIZE, TILE_SIZE);
        for(int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++){
                if(cellmap[x][y]){
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(new StaticTiledMapTile(randomFloorTile()));
                    terrainLayer.setCell(x, y, cell);
                }else{ // is wall
                    placeWall(x, y);
                }
                if(y == 0){
                    placeWall(x, y - 1);
                }
                if(x == 0){
                    placeWall(x - 1, y);
                }
                if(y == mapHeight - 1){
                    placeWall(x, y + 1);
                }
                if(x == mapWidth - 1){
                    placeWall(x + 1, y);
                }
            }
        }
    }

    public Array<Enemy> spawnEnemies(int number){
        System.out.printf("Spawning %d enemies \n", number);
        Array<Enemy> array = new Array<Enemy>();
        for( int i = 0; i < number; i++){
            array.add( new Enemy(screen, getRandomPlace()) );
        }
        return array;
    }


    public Vector2 getTreasureSpot(){
        //adjacent wall tiles
        int treasureHiddenLimit = 6;
        for (int x=0; x < mapWidth; x++){
            for (int y=0; y < mapHeight; y++){
                if(cellmap[x][y]){
                    int nbs = countAliveNeighbours(cellmap, x, y);
                    if(nbs >= treasureHiddenLimit){
                        return new Vector2(x, y);
                    }
                }
            }
        }
        return new Vector2(0,0);
    }

    public Vector2 getRandomPlace(){
        int randomX, randomY;
        System.out.println(new Vector2(mapWidth, mapHeight));

        while(true){
            randomX = random(mapWidth - 1);
            randomY = random(mapHeight - 1);
            System.out.println(new Vector2(randomX, randomY));
            if(cellmap[randomX][randomY]){
                return new Vector2((float) randomX, (float) randomY);
            }
        }
    }

    private void createGoal(){
        BodyDef bdef = new BodyDef();
        Vector2 goalPosition = getTreasureSpot();
        bdef.position.set((goalPosition.x + 0.5f) * 20 / PPM, (goalPosition.y + 0.5f) * 20 / PPM);
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
//        wallTexture = splitTiles[3][17];
    }


    private TextureRegion randomFloorTile(){
        return floorTiles.random();
    }
}
