package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.*;
import com.test.test.models.*;
import com.test.test.DankDungeon;
import com.test.test.scenes.GameHud;
import com.test.test.utils.CaveGenerator;
import com.test.test.utils.WorldContactListener;

import static com.test.test.DankDungeon.PPM;
import static com.test.test.DankDungeon.V_HEIGHT;
import static com.test.test.DankDungeon.V_WIDTH;

/**
 * The main game Screen.
 */
public class GameScreen implements Screen {
    // game
    private DankDungeon game;

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

    private Array<B2DSprite> entityList;
    private Array<B2DSprite> deleteList;

    // tools
    private AssetManager assetManager;
    private CaveGenerator caveGen;
    private int floor;

    private Music bgm;

    private TextureRegion[][] powerTiles;

    public static final int TILE_SIZE = 24;

    private boolean levelUp;

    public GameScreen(DankDungeon game, AssetManager manager){
        this.floor = 1;
        this.levelUp = false;
        this.game = game;
        this.world = new World(new Vector2(0, 0), true);
        world.setContactListener(new WorldContactListener(this));
        this.b2dr = new Box2DDebugRenderer();
        this.cam = new OrthographicCamera();
        cam.setToOrtho(false, V_WIDTH / 2 / PPM, V_HEIGHT / 2 / PPM);
        this.assetManager = manager;

        bgm = assetManager.get("sounds/main-loop-100.ogg", Music.class);
        this.atlas = new TextureAtlas("animations/entities.pack");
        this.powerTiles = TextureRegion.split(new Texture("textures/items.png"), 25, 25);
        this.gamePort = new ExtendViewport(DankDungeon.V_WIDTH / PPM, DankDungeon.V_HEIGHT / PPM , cam);
        this.hud = new GameHud(game.batch, assetManager.get("ui/skin.json", Skin.class));
        this.entityList = new Array<B2DSprite>();
        this.deleteList = new Array<B2DSprite>();
        this.caveGen = new CaveGenerator(this);
        this.player = new Hero(this, new Vector2(0, 0));
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);
        playMusic("main-loop-100");

        Cursor customCursor = Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("textures/crosshair.png")), 32, 32);
        Gdx.graphics.setCursor(customCursor);
        customCursor.dispose();

        generateLevel(floor);

        // set camera
        cam.position.set(gamePort.getWorldWidth() / PPM, gamePort.getWorldHeight() / 2 / PPM, 0);
        cam.zoom -= 0.7f;
        mapRenderer.setView(cam);
    }

    private void playMusic(String musicID) {
        if (bgm.isPlaying()) {
            bgm.stop();
        }
        bgm = assetManager.get("sounds/" + musicID + ".ogg", Music.class);
        bgm.setLooping(true);
        bgm.setVolume(0.3f);
        bgm.play();
    }

    public void gameOver(){
        int score = hud.getScore();
        dispose();
        game.setScreen(new GameOverScreen(game, score, assetManager));
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

        // update world entities
        for( B2DSprite b : entityList){
             b.update(delta);
            if( b instanceof Enemy){
                ((Enemy) b).setTarget(player.getPosition().cpy());
            }
        }

        // camera offset slightly towards cursor
        Vector2 pos = new Vector2();
        pos.clamp(-0.1f, 0.1f);
        pos = cursorBody.getPosition().cpy().sub(player.getPosition()).nor().scl(0.05f);
        cam.position.x = player.getPosition().x;
        cam.position.y = player.getPosition().y;
        cam.translate(pos);
        cam.update();
        hud.update(delta);
        mapRenderer.setView(cam);

        // draw the game
        draw();

        if(player.isSetToDestroy()){
            gameOver();
        }
    }

    private void draw(){
        Gdx.gl.glClearColor(50f / 255f, 50f / 255f, 50f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();

        if(Gdx.input.isKeyPressed(Input.Keys.NUM_1)){
            b2dr.render(world, cam.combined);
        }

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        hud.draw(game.batch);
        player.render(game.batch);
        for(B2DSprite sprite : entityList){
            sprite.render(game.batch);
        }
        game.batch.end();

        // draw hud
        game.batch.setProjectionMatrix(hud.getStage().getCamera().combined);
        hud.getStage().draw();
    }

    private void deleteUselessBodies(){
        for( B2DSprite entity : entityList ){
            if (entity.isSetToDestroy() && !entity.isDestroyed()){
                deleteList.add(entity);
            }
        }

        for( B2DSprite b : deleteList ){
            if( b instanceof Enemy ){
                // create a pool of blood on the tile they died on
                Vector2 c = CaveGenerator.worldPositionToCell(b.getPosition());
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(powerTiles[2][8]));
                ((TiledMapTileLayer) map.getLayers().get("objects")).setCell(Math.round(c.x), Math.round(c.y), cell);
                // give the player score based on the enemy
                hud.updateScore(((Enemy) b).getScoreValue());
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
        if(cursorBody != null) world.destroyBody(cursorBody);
        defineCursorBody();

        // previous floor needs to be cleaned up
        if(floor > 1) caveGen.destroyLevel();

        this.map = caveGen.generateCave(floor);
        mapRenderer.setMap(map);
        player.redefine(CaveGenerator.cellToWorldPosition(caveGen.getHeroSpawn()));
    }

    public void levelUp(){
        levelUp = true;
    }

    private void newFloor(){
        floor++;
        if(floor == 5){
            playMusic("main-loop-110");
        }else if(floor == 10){
            playMusic("main-loop-120");
        }else if(floor == 15){
            playMusic("main-loop-130");
        }else if(floor == 20){
            playMusic("main-loop-140");
        }else if(floor == 25){
            playMusic("main-loop-150");
        }
        hud.setFloor(floor);
        player.unblock();
        world.clearForces();
        for( B2DSprite entity : entityList ){
            if (!entity.isDestroyed()){
                world.destroyBody(entity.getBody());
            }
        }
        entityList.clear();
        generateLevel(floor);
    }

    public void add(B2DSprite b){
        entityList.add(b);
    }
    public void add(Array<? extends B2DSprite> bodyList){
        entityList.addAll(bodyList);
    }

    // cheats - remove
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

        fdef.filter.categoryBits = 0;
        fdef.filter.maskBits = 0;

        this.cursorBody = world.createBody(bdef);
        cursorBody.createFixture(fdef).setUserData("cursor");
    }

    private static final float STEP_TIME = 1f / 60f;
    private static final int VELOCITY_ITERATIONS = 8;
    private static final int POSITION_ITERATIONS = 3;
    private float accumulator = 0;

    private void stepWorld() {
        accumulator += Math.min(Gdx.graphics.getDeltaTime(), 0.25f);

        if (accumulator >= STEP_TIME) {
            accumulator -= STEP_TIME;

            world.step(STEP_TIME, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
        }
    }

    @Override
    public void resize(int width, int height){
        gamePort.update(width, height);
        hud.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
//        dispose();
    }

    @Override
    public void dispose() {
        bgm.stop();
        bgm.dispose();
        Timer.instance().clear();
        world.clearForces();
        for( B2DSprite entity : entityList ){
            if (!entity.isDestroyed()){
                world.destroyBody(entity.getBody());
            }
        }
        entityList.clear();
        caveGen.destroyLevel();
        map.dispose();
        world.destroyBody(cursorBody);
        player.dispose();
        world.destroyBody(player.getBody());
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public GameHud getHud(){ return this.hud; }
    public World getWorld(){ return this.world; }
    public TiledMap getMap(){ return this.map; }
    public Hero getPlayer(){ return this.player; }
    public Body getCursor() { return this.cursorBody; }
    public boolean[][] getLevelMap(){ return caveGen.getCellMap(); }
    public TextureAtlas getAtlas(){ return this.atlas; }
    public AssetManager getAssetManager(){ return this.assetManager; }
    public OrthographicCamera getCam() { return this.cam; }
}
