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
import com.badlogic.gdx.utils.Queue;
import com.test.test.models.*;
import com.test.test.screens.GameScreen;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.test.test.DankDungeon.PPM;
import static com.test.test.screens.GameScreen.TILE_SIZE;
import static com.test.test.utils.WorldContactListener.*;

/**
 * Created by fionn on 07/01/17.
 */
public class CaveGenerator {

    // matrix of the current floor's cells
    private boolean[][] caveCells;

    // cellular automata constants
    private static final float INITIAL_WALL_CHANCE = 0.40f;
    private static final float MINIMUM_AREA_COVERAGE = 0.45f;
    private static final int BIRTH_LIMIT = 6;      // become a wall if neighbouring this many walls
    private static final int SURVIVE_LIMIT = 3;    // if surrounded by under this much, become a floor
    private static final int SIMULATION_STEPS = 6;

    private int mapWidth;
    private int mapHeight;

    private TiledMapTileLayer terrainLayer;
    private GameScreen screen;
    private B2DSprite goal;
    private TiledMap map;

    private Array<TextureRegion> floorTiles;
    private Array<Body> wallBodies;

    private TextureRegion goalTexture;


    public CaveGenerator(GameScreen screen, Texture worldTileSheet){
        this.screen = screen;
        this.wallBodies = new Array<Body>();
        this.map = new TiledMap();
        this.floorTiles = new Array<TextureRegion>();


        TextureRegion[][] splitTiles = TextureRegion.split(worldTileSheet, TILE_SIZE, TILE_SIZE);
        for(int x = 0; x < splitTiles[0].length; x++){
            floorTiles.add(splitTiles[0][x]);
        }

        goalTexture = splitTiles[1][7];

    }

    /**
     * Generates a map with given dimensions.
     * @param seed A random number between 0 and 1 that decides the cave size.
     * @return A TiledMap with the new cavern as a Layer.
     */
    public TiledMap generateCave(float seed){
        int minimumSize = (10 + Math.round(seed * 15));
        mapWidth = mapHeight = Math.round(seed * 64) + minimumSize;
        System.out.printf("Generating new cave - %d x %d   (min - %d) \n", mapWidth, mapHeight, minimumSize);

        boolean[][] optimisedCave;
        // keep generating caves until floor area is at least 45% of map
        do{
            initialiseMap();
            simulateCave();
            optimisedCave = floodFill();
        }while(numFloorTiles(optimisedCave) < Math.round(mapWidth * mapHeight * MINIMUM_AREA_COVERAGE));

        caveCells = optimisedCave;

        defineLevel();
        createGoal();
        map.getLayers().add(terrainLayer);
        return map;
    }

    public Array<Enemy> generateEnemies(float seed){
        Array<Enemy> array = new Array<Enemy>();

        int numSkels = Math.round(MathUtils.sin(seed * seed) * 100);
        int numRoaches = Math.round(MathUtils.sin((seed * seed) / 2) * 50);
        int numWolves = (seed > 0.2f) ? Math.round((seed/2.0f) * (seed - 0.2f) * 25) : 0;

        numSkels = 0;
        numWolves = 1;
        numRoaches = 0;
        System.out.printf("SEED: %f   (%d/%d/%d) \n", seed, numSkels, numRoaches, numWolves);

        for( int i = 0; i < numSkels; i++){
            array.add( new Skeleton(screen, cellToWorldPosition(getRandomPlace())) );
        }
        for( int i = 0; i < numRoaches; i++){
            array.add( new Shooter(screen, cellToWorldPosition(getRandomPlace())) );
        }
        for( int i = 0; i < numWolves; i++){
            array.add( new Wolf(screen, cellToWorldPosition(getRandomPlace())) );
        }

        return array;
    }

    /**
     * Gets a random floor tile that is surrounded by a given number of walls.
     * @param rarity How many walls need to be surrounding the tile.
     * @return A Vector2 of the position.
     */
    public Vector2 getTreasureSpot(int rarity){
        Vector2 position;
        boolean spotFound = false;
        int x, y;
        do{
            position = getRandomPlace();
            x = Math.round(position.x);
            y = Math.round(position.y);
            if(!caveCells[x][y]){
                int surroundingWalls = countAliveNeighbours(caveCells, x, y, 1);
                if(surroundingWalls >= rarity){
                    spotFound = true;
                }
            }
        }while(!spotFound);
        return position;
    }

    public boolean[][] getCellMap(){ return caveCells; }

    /**
     * Finds a random floor tile in the cave.
     * @return A Vector2 of the floor tile.
     */
    public Vector2 getRandomPlace(){
        int randomX, randomY;
        while(true){
            randomX = random(mapWidth - 1);
            randomY = random(mapHeight - 1);
            if(!caveCells[randomX][randomY]){
                return new Vector2((float) randomX, (float) randomY);
            }
        }
    }

    /**
     * Translates from cells or tiles on the map to Box2D world co-ordinates.
     * @param cellPosition
     * @return
     */
    public static Vector2 cellToWorldPosition(Vector2 cellPosition){
        return new Vector2((cellPosition.x + 0.5f) * TILE_SIZE / PPM, (cellPosition.y + 0.5f) * TILE_SIZE / PPM);
    }

    /**
     * Translates from Box2D world co-ordinates to it's corresponding cell on the map.
     * @param worldPosition The box2d body's world co-ordinates.
     * @return
     */
    public static Vector2 worldPositionToCell(Vector2 worldPosition){
        worldPosition = worldPosition.cpy().scl(PPM / TILE_SIZE);
        return new Vector2(Math.round(worldPosition.x - 0.5f), Math.round(worldPosition.y - 0.5f));
    }

    /**
     * Tear down the current level, including any box2d bodies created for cave walls.
     */
    public void destroyLevel(){
        for(Body b : wallBodies){
            screen.getWorld().destroyBody(b);
        }
        wallBodies.clear();
        screen.getWorld().destroyBody(goal.getBody());
        screen.getMap().getLayers().remove(terrainLayer);
        for(int x=0; x < mapWidth; x++){
            for(int y=0; y < mapHeight; y++){
                caveCells[x][y] = false;
            }
        }
    }

    /**
     * Counts the number of floor tiles on a given cave map.
     * @param map The 2D array of cells to check.
     * @return Number of floor tiles in the map.
     */
    private int numFloorTiles(boolean[][] map) {
        int count = 0;
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (!map[x][y]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Fill the matrix with walls given random chance.
     */
    private void initialiseMap(){
        caveCells = new boolean[mapWidth][mapHeight];
        for(int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++){
                if(random() < INITIAL_WALL_CHANCE){
                    caveCells[x][y] = true;
                }
            }
        }
    }

    /**
     * Step the cave cellular automata simulation for the desired number of steps.
     */
    private void simulateCave(){
        for(int i=0; i< SIMULATION_STEPS; i++){
            boolean[][] newMap = new boolean[mapWidth][mapHeight];

            for(int x = 0; x < mapWidth; x++){
                for(int y = 0; y < mapHeight; y++){
                    int surroundingWalls = this.countAliveNeighbours(caveCells, x, y, 1);
                    if(caveCells[x][y]){
                        // keep wall if surrounded by minimum number of walls
                        newMap[x][y] = (surroundingWalls >= SURVIVE_LIMIT);
                    }else{
                        // create wall if surrounded by minimum number of walls
                        newMap[x][y] = (surroundingWalls >= BIRTH_LIMIT);
                    }
                }
            }
            caveCells = newMap;
        }
    }

    /**
     * Returns a copy of the current cave with one single cavern. (All areas reachable)
     * @return New 2D array of boolean cells for the cave
     */
    private boolean[][] floodFill(){
        boolean[][] cavern = new boolean[mapWidth][mapHeight];
        // initialise new cavern to all walls
        for(int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++) {
                cavern[x][y] = true;
            }
        }

        Queue<Cell> cellQueue = new Queue<Cell>();
        cellQueue.addFirst(new Cell(getRandomPlace()));
        Cell cell;

        while(cellQueue.size > 0){
            cell = cellQueue.removeFirst();
            if(cavern[cell.x][cell.y]){
                cavern[cell.x][cell.y] = false;

                // if neighbours are in bounds, are floor tiles and not already checked - add them
                if(cell.x > 0 && !caveCells[cell.x - 1][cell.y] && cavern[cell.x - 1][cell.y]){
                    cellQueue.addLast(new Cell(new Vector2(cell.x - 1, cell.y)));
                }
                if(cell.x != (mapWidth - 1) && !caveCells[cell.x + 1][cell.y] && cavern[cell.x + 1][cell.y]){
                    cellQueue.addLast(new Cell(new Vector2(cell.x + 1, cell.y)));
                }
                if(cell.y > 0 && !caveCells[cell.x][cell.y - 1] && cavern[cell.x][cell.y - 1]){
                    cellQueue.addLast(new Cell(new Vector2(cell.x, cell.y - 1)));
                }
                if(cell.y != (mapHeight - 1) && !caveCells[cell.x][cell.y + 1] && cavern[cell.x][cell.y + 1]){
                    cellQueue.addLast(new Cell(new Vector2(cell.x, cell.y + 1)));
                }
            }

        }

        return cavern;
    }

    /**
     * Counts the number of walls surrounding a cell in the given radius.
     */
    private int countAliveNeighbours(boolean[][] map, int x, int y, int radius){
        int count = 0;
        for(int iX = x - radius; iX <= (x + radius); iX++){
            for(int iY = y - radius; iY <= (y + radius); iY++){
                // We don't count the (x,y) square
                if(!(iX == x && iY == y)){
                    // in case we are checking outside array bounds, count as wall.
                    if(iX < 0 || iY < 0 || iX >= map.length || iY >= map[0].length){
                        count = count + 1;
                    }else if(map[iX][iY]){
                        count = count + 1;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Creates the terrain bodies and textures for the cave.
     */
    private void defineLevel(){
        terrainLayer = new TiledMapTileLayer(mapWidth, mapHeight, TILE_SIZE, TILE_SIZE);
        for(int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++){
                if(!caveCells[x][y]){
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(new StaticTiledMapTile(randomFloorTile()));
                    terrainLayer.setCell(x, y, cell);
                }else{ // is wall
                    placeWall(x, y);
                }
                // place border walls
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

    /**
     * Create the cave level exit, ensuring to be at least a certain distance from the player.
     */
    private void createGoal(){
        BodyDef bdef = new BodyDef();

        Vector2 goalPosition = getTreasureSpot(5);
        bdef.position.set(cellToWorldPosition(goalPosition));
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
        fdef.filter.categoryBits = PICKUP;
        fdef.filter.maskBits = PLAYER;

        Body b2body = screen.getWorld().createBody(bdef);
        b2body.createFixture(fdef).setUserData("goal");
        this.goal = new B2DSprite(b2body);
        b2body.setUserData(goal);

        // replace floor texture with goal texture
        terrainLayer.getCell(Math.round(goalPosition.x), Math.round(goalPosition.y)).getTile().setTextureRegion(goalTexture);
    }

    private void placeWall(int x, int y){
        Vector2 centre = new Vector2((x + 0.5f) * TILE_SIZE / 2 / PPM,
                (y + 0.5f) * TILE_SIZE / 2 / PPM);
        BodyDef bdef = new BodyDef();
        PolygonShape wall = new PolygonShape();
        FixtureDef fdef = new FixtureDef();

        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(centre);
        wall.setAsBox(TILE_SIZE / 2 / PPM, TILE_SIZE / 2 / PPM, centre, 0);
        fdef.shape = wall;
        fdef.filter.categoryBits = WALL;
        fdef.filter.maskBits = PLAYER | PLAYER_PROJECTILE | ENEMY_PROJECTILE | ENEMY;

        Body body = screen.getWorld().createBody(bdef);
        body.createFixture(fdef).setUserData("wall");
        wallBodies.add(body);

        wall.dispose();
        /* SET WALL TEXTURE */
//        wallTexture = splitTiles[3][17];
    }

    private TextureRegion randomFloorTile(){
        return floorTiles.random();
    }


    /**
     * Cell class - just a wrapper for a Vector2.
     */
    private class Cell{
        private int x;
        private int y;

        private Cell(Vector2 position){
            x = Math.round(position.x);
            y = Math.round(position.y);
        }
    }

}
