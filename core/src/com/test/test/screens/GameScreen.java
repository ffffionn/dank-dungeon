package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.models.*;
import com.test.test.SpaceAnts;
import com.test.test.scenes.GameHud;
import com.test.test.utils.CaveGenerator;
import com.test.test.utils.WorldContactListener;

import static com.test.test.SpaceAnts.PPM;
import static com.test.test.SpaceAnts.V_HEIGHT;
import static com.test.test.SpaceAnts.V_WIDTH;

/**
 * Created by Fionn on 22/10/2016.
 */
public class GameScreen implements Screen {
    // game
    private SpaceAnts game;

    // screen
    private OrthographicCamera cam;
    private Viewport gamePort;
    private GameHud hud;

    private TextureAtlas atlas;

    // map
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    // box2d
    private World world;
    private Box2DDebugRenderer b2dr;


    // objects
    private Hero player;
    private Body cursorBody;

    private Texture tiles;
    private Array<Enemy> enemies;
    private float modifier;

    private Array<B2DSprite> entityList;
    private Array<B2DSprite> deleteList;

    // tools
    private AssetManager assetManager;
    private CaveGenerator levelGen;
    private int floor;

    private boolean levelUp;

    public GameScreen(SpaceAnts game){
        this.game = game;
        this.world = new World(new Vector2(0, 0), true);
        this.b2dr = new Box2DDebugRenderer();
        this.cam = new OrthographicCamera();
        cam.setToOrtho(false, V_WIDTH / 2 / PPM, V_HEIGHT / 2 / PPM);
        this.floor = 1;
        this.levelUp = false;
        world.setContactListener(new WorldContactListener(this));
        this.modifier = 1.0f;
        this.assetManager = new AssetManager();
        this.enemies = new Array<Enemy>();
        this.atlas = new TextureAtlas("animations/player.pack");
        this.gamePort = new StretchViewport(SpaceAnts.V_WIDTH / PPM, SpaceAnts.V_HEIGHT / PPM , cam);
//        this.tiles = new Texture(Gdx.files.internal("textures/dungeon_tiles2.png"));
        this.tiles = new Texture("textures/dungeon_tiles2.png");
        this.hud = new GameHud(game.batch);
        this.entityList = new Array<B2DSprite>();
        this.deleteList = new Array<B2DSprite>();
        this.levelGen = new CaveGenerator(this, tiles);
        this.player = new Hero(this, new Vector2(0, 0));
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);

        generateLevel(floor);

        // set camera
        cam.position.set(gamePort.getWorldWidth() / PPM, gamePort.getWorldHeight() / 2 / PPM, 0);
        cam.zoom -= 0.6;
        mapRenderer.setView(cam);

        // set player animation frames
        TextureAtlas.AtlasRegion region = atlas.findRegion("player-move");
        TextureRegion[] moveFrames = region.split(64, 64)[0];
        player.setTexture(moveFrames[0]);
        player.setAnimation(moveFrames, 1 / 12f);
    }

    @Override
    public void render(float delta){
        if(levelUp){
            levelUp = false;
            newFloor();
        }
        stepWorld();
        deleteUselessBodies();
        handleInput(delta);

        player.update(delta);
        updateCursorBody();

        // update enemies
        for( Enemy e : enemies ){
            e.setTarget(player.getPosition());
            e.update(delta);
        }

        // keep camera centered on player
        cam.position.x = player.getPosition().x;
        cam.position.y = player.getPosition().y;
        cam.update();
        mapRenderer.setView(cam);

        // draw the game
        Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 100f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();
        b2dr.render(world, cam.combined);

        game.batch.setProjectionMatrix(hud.getStage().getCamera().combined);
        hud.getStage().draw();
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        player.render(game.batch);
        for(B2DSprite sprite : entityList){
//            sprite.render(game.batch);
        }
        game.batch.end();
    }

    private void deleteUselessBodies(){
        for( B2DSprite entity : entityList ){
            if (entity.isSetToDestroy() && !entity.isDestroyed()){
                deleteList.add(entity);
            }
        }

        for( B2DSprite b : deleteList ){
            if( b instanceof Enemy ){
                // give the player score based on the enemy
                hud.updateScore(((Enemy) b).getScoreValue());
                enemies.removeValue((Enemy) b, true);
            }
            if( b instanceof Barrier){
                // start cooldown?
            }
            world.destroyBody(b.getBody());
            b.dispose();
        }
        entityList.removeAll(deleteList, true);
        deleteList.clear();
    }

    @Override
    public void show() {
    }

    private void updateCursorBody(){
        Vector3 v3 = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        cursorBody.setTransform(new Vector2(v3.x, v3.y), 0);
    }

    private void generateLevel(int level){
        if(cursorBody != null){
            world.destroyBody(cursorBody);
        }
        defineCursorBody();
        System.out.printf(" ***LEVEL %d*** \n", level);
        float seedFloor = (level % 5) / 10.0f;
        float seedCeiling = ((level % 10) + 1) / 10.0f;
        float seed = MathUtils.random(seedFloor, seedCeiling);


        if(floor > 1) levelGen.destroyLevel();
        this.map = levelGen.generateCave(Math.round(seed * 64) + 10, Math.round(seed * 64) + 10, seed);

        int numEnemies = Math.round(seed * 150);
        numEnemies = 1;

        System.out.printf("Picking seed (%f) from between - (%f, %f) \n", seed, seedFloor, seedCeiling);

        player.redefine(levelGen.getRandomPlace());
        enemies = levelGen.spawnEnemies(numEnemies);
        entityList.addAll(enemies);

        mapRenderer.setMap(map);
    }

    public void levelUp(){
        levelUp = true;
    }

    private void newFloor(){
        floor++;
        hud.setFloor(floor);
        player.unblock();
        world.clearForces();
        for( B2DSprite entity : entityList ){
            if (!entity.isDestroyed()){
                world.destroyBody(entity.getBody());
            }
        }
        entityList.clear();
        enemies.clear();
        generateLevel(floor);
    }

    public void add(B2DSprite b){
        entityList.add(b);
    }

    public void handleInput(float dt){
        if (Gdx.input.isKeyPressed(Input.Keys.Q)){
            cam.zoom -= 0.1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)){
            cam.zoom += 0.1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)){
            levelUp();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)){
            System.out.println(player.getCurrentState().toString());
        }
    }

    private void defineCursorBody(){
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

        this.cursorBody = world.createBody(bdef);
        cursorBody.createFixture(fdef);
    }

    private static final float STEP_TIME = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 8;
    private static final int POSITION_ITERATIONS = 3;
    private float accumulator = 0;

    private void stepWorld() {
        float delta = Gdx.graphics.getDeltaTime();

        accumulator += Math.min(delta, 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;

            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }

    @Override
    public void resize(int width, int height){
        gamePort.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        System.out.println("bye bye");
        dispose();
    }

    @Override
    public void dispose() {
        deleteUselessBodies();
        levelGen.destroyLevel();
        map.dispose();
        mapRenderer.dispose();
        world.dispose();
        b2dr.dispose();
//        hud.dispose();
    }

    public GameHud getHud(){
        return this.hud;
    }

    public TextureAtlas getAtlas(){ return this.atlas; }

    public World getWorld(){
        return this.world;
    }

    public TiledMap getMap(){
        return this.map;
    }

    public Body getCursor() { return this.cursorBody; }
    public OrthographicCamera getCam() { return this.cam; }

}
