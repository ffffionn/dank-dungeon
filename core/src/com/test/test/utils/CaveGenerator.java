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
import com.test.test.models.B2DSprite;
import com.test.test.models.Enemy;
import com.test.test.screens.GameScreen;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.test.test.SpaceAnts.PPM;

/**
 * Created by fionn on 07/01/17.
 */
public class CaveGenerator {

    private static final int TILE_SIZE = 20;

    private static final float INITIAL_WALL_CHANCE = 0.40f;
    private static final float MINIMUM_AREA_COVERAGE = 0.45f;

    private static final int BIRTH_LIMIT = 6;
    private static final int SURVIVE_LIMIT = 3;
    private static final int SIMULATION_STEPS = 6;

    private int mapWidth;
    private int mapHeight;

    private TiledMapTileLayer terrainLayer;
    private GameScreen screen;
    private B2DSprite goal;
    private TiledMap map;

    private Array<TextureRegion> floorTiles;
    private Array<Body> wallBodies;

    private boolean[][] caveCells;

    public CaveGenerator(GameScreen screen, Texture worldTileSheet){
        this.screen = screen;
        this.wallBodies = new Array<Body>();
        this.map = new TiledMap();
        this.floorTiles = new Array<TextureRegion>();

        TextureRegion[][] splitTiles = TextureRegion.split(worldTileSheet, TILE_SIZE, TILE_SIZE);
        for(int x = 1; x <= 2; x++){
            for( int y = 1; y <= 2; y++){
                floorTiles.add(splitTiles[x][y]);
            }
        }
    }

    public TiledMap generateMap(int width, int height, float seed){
        System.out.printf("Generating new cave - %d x %d  \n", width, height);

        mapHeight = height;
        mapWidth = width;

        boolean[][] optimisedCave;
        do{
            initialiseMap();
            simulateCave();
            optimisedCave = floodFill();
        }while( numFloorTiles(optimisedCave) < Math.round(mapWidth * mapHeight * MINIMUM_AREA_COVERAGE));

        caveCells = optimisedCave;

        defineLevel();
        createGoal();
        map.getLayers().add(terrainLayer);
        return map;
    }

    public void destroyLevel(){
        for(Body b : wallBodies){
            screen.getWorld().destroyBody(b);
        }
        wallBodies.clear();
        screen.getWorld().destroyBody(goal.getBody());
        screen.getMap().getLayers().remove(terrainLayer);
        System.out.printf("-- %d bodies left after level tear down --\n", screen.getWorld().getBodyCount());
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
                        if(surroundingWalls < SURVIVE_LIMIT) {
                            newMap[x][y] = false;
                        }else{
                            newMap[x][y] = true;
                        }
                    }else{   // floor cell
                        if(surroundingWalls >= BIRTH_LIMIT){
                            newMap[x][y] = true;
                        }else{
                            newMap[x][y] = false;
                        }
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
        // initialise cavern to all walls
        for(int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++) {
                cavern[x][y] = true;
            }
        }

        Queue<Cell> cellQueue = new Queue<Cell>();
        cellQueue.addFirst(new Cell(getRandomPlace()));
        Cell cell;

        System.out.println("Begin loop");
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

    private class Cell{
        private int x;
        private int y;

        private Cell(Vector2 position){
            x = Math.round(position.x);
            y = Math.round(position.y);
//            System.out.printf("%s  ->  (%d, %d) \n", position, x, y);
        }
    }

    private boolean[][] doSimulationStep(boolean[][] oldMap){
        boolean[][] newMap = new boolean[mapWidth][mapHeight];

        for(int x = 0; x < mapWidth; x++){
            for(int y = 0; y < mapHeight; y++){
                int surroundingWalls = this.countAliveNeighbours(oldMap, x, y, 1);
                if(oldMap[x][y]){
                    if(surroundingWalls < SURVIVE_LIMIT ){
                        newMap[x][y] = false;
                    }else{
                        newMap[x][y] = true;
                    }
                }else{
                    if(surroundingWalls >= BIRTH_LIMIT){
                        newMap[x][y] = true;
                    }else{
                        newMap[x][y] = false;
                    }
                }
            }
        }
        return newMap;
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
                // place surrounding wall
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
     * Spawns the given number of enemies in random positions in the cave floor.
     * @param number Number of enemies to spawn.
     * @return An Array of Enemies.
     */
    public Array<Enemy> spawnEnemies(int number){
        Array<Enemy> array = new Array<Enemy>();
        for( int i = 0; i < number; i++){
            array.add( new Enemy(screen, getRandomPlace()) );
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
     * Create the cave level exit, ensuring to be at least a certain distance from the player.
     */
    private void createGoal(){
        BodyDef bdef = new BodyDef();

        Vector2 goalPosition = getTreasureSpot(5);


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
