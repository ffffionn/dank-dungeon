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
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.models.Enemy;
import com.test.test.models.Hero;
import com.test.test.SpaceAnts;
import com.test.test.scenes.GameHud;
import com.test.test.utils.LevelDefiner;
import com.test.test.utils.LevelGenerator;
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

    private AssetManager assetManager;
    private LevelDefiner ld;
    private LevelGenerator levelGen;
    private int floor;

    public GameScreen(SpaceAnts game){
        this.game = game;
        this.world = new World(new Vector2(0, 0), true);
        this.b2dr = new Box2DDebugRenderer();
        this.cam = new OrthographicCamera();
        cam.setToOrtho(false, V_WIDTH / 2 / PPM, V_HEIGHT / 2 / PPM);
        this.floor = 1;
        world.setContactListener(new WorldContactListener(world, this));
        this.modifier = 1.0f;
        this.assetManager = new AssetManager();
        this.map = new TiledMap();
        this.enemies = new Array<Enemy>();
        this.atlas = new TextureAtlas("animations/player.pack");
        this.gamePort = new FitViewport(SpaceAnts.V_WIDTH / PPM, SpaceAnts.V_HEIGHT / PPM , cam);
        this.tiles = new Texture(Gdx.files.internal("textures/dungeon_tiles2.png"));
        this.hud = new GameHud(game.batch);
        this.ld = new LevelDefiner(world, this);
        this.cursorBody = ld.defineCursor();
        this.levelGen = new LevelGenerator(this, tiles);
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
        stepWorld();
        handleInput(delta);

        player.update(delta);

//        if (player.isDead()){
//            game.setScreen(new GameOverScreen());
//        }

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

        // openGL
        Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 100f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();
        b2dr.render(world, cam.combined);

        game.batch.setProjectionMatrix(hud.getStage().getCamera().combined);
        hud.getStage().draw();
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        player.render(game.batch);
        game.batch.end();
    }

    @Override
    public void show() {

    }

    public void generateLevel(int level){
        System.out.printf("gen level %d  \n", level);

        float seedFloor = (level % 10) / 100.0f;
        float seedCeiling = (level / 100.0f);

        System.out.printf("R - (%f, %f) \n", seedFloor, seedCeiling);

        float seed = MathUtils.random(seedFloor, seedCeiling);

        if(floor > 1)levelGen.destroyLevel();

        this.map = levelGen.generateLevel(64, 64, seed);

//        this.player = ld.defineHero(Math.round(start.x), Math.round(start.y));
        System.out.println("hmmm");
        player.redefine(levelGen.getHeroStart());
        System.out.println("yepp!");

//        System.out.println("goal!");
//
//        Vector2 goal = levelGen.getGoalTile();
//        // floats?
//        ld.defineGoal(Math.round(goal.x), Math.round(goal.y));

        System.out.println("enemies!");

        Vector2 start;
        for(int i=0; i < 20; i++){
            start = levelGen.getRandomTile();
            enemies.add(ld.defineEnemy(Math.round(start.x), Math.round(start.y)));
        }

        System.out.println("done!");

        mapRenderer.setMap(map);
    }


    public void levelUp(){
        System.out.println("level up!!");
        System.out.printf(" %d  enemies left \n", enemies.size);
        floor++;
        world.clearForces();
        for( Enemy e : enemies ){
            if (!e.isDead()){
                e.dispose();
                System.out.println(e.toString());
            }
        }
        this.enemies = new Array<Enemy>();
        generateLevel(floor);
    }

    public void handleInput(float dt){
        faceCursor();
        if (Gdx.input.isKeyPressed(Input.Keys.Q)){
            cam.zoom -= 0.1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)){
            cam.zoom += 0.1f;
        }
    }

    private void faceCursor(){
        // get the game-world translation of the mouse position
        Vector3 v3 = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float x = v3.x;
        float y = v3.y;
        float angle = MathUtils.atan2(y - player.getPosition().y, x - player.getPosition().x);

        cursorBody.setTransform(new Vector2(x, y), 0);
        player.getBody().setTransform(player.getPosition(),
                                   MathUtils.atan2(y - player.getPosition().y,
                                                   x - player.getPosition().x));
        player.getSprite().setRotation(angle * MathUtils.radiansToDegrees);
    }


    static final float STEP_TIME = 1f / 60f;
    static final int VELOCITY_ITERATIONS = 6;
    static final int POSITION_ITERATIONS = 2;

    float accumulator = 0;

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

}
