package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
 * The main Game Loop that updates the entities and draw them.
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

    // for pausing the game
    private enum State{ RUNNING, PAUSED }
    private State state;
    private Stage pauseMenu;

    public GameScreen(DankDungeon game, AssetManager manager){
        this.game = game;
        this.assetManager = manager;
        this.floor = 1;
        this.levelUp = false;
        this.entityList = new Array<B2DSprite>();
        this.deleteList = new Array<B2DSprite>();
        this.state = State.RUNNING;

        // set up box2d physics world
        this.world = new World(new Vector2(0, 0), true);
        world.setContactListener(new WorldContactListener(this));
        this.b2dr = new Box2DDebugRenderer(); // todo: remove

        this.cam = new OrthographicCamera();
        cam.setToOrtho(false, V_WIDTH / 2 / PPM, V_HEIGHT / 2 / PPM);
        this.gamePort = new ExtendViewport(DankDungeon.V_WIDTH / PPM, DankDungeon.V_HEIGHT / PPM , cam);

        this.atlas = assetManager.get("textures/worldTextures.pack");
        this.caveGen = new CaveGenerator(this);
        this.powerTiles = atlas.findRegion("items").split(25, 25);
        this.hud = new GameHud(game.batch, assetManager.get("ui/ui_skin.json", Skin.class));
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);
        this.player = new Hero(this, new Vector2(0, 0));

        // set our custom crosshair
        Cursor customCursor = Gdx.graphics.newCursor(assetManager.get("textures/crosshair.png", Pixmap.class), 32, 32);
        Gdx.graphics.setCursor(customCursor);
        customCursor.dispose();

        this.bgm = assetManager.get("sounds/main-loop-100.ogg", Music.class);
        playMusic("main-loop-100");
        generateLevel(floor);

        // set camera
        cam.position.set(gamePort.getWorldWidth() / PPM, gamePort.getWorldHeight() / 2 / PPM, 0);
        cam.zoom -= 0.7f;
        mapRenderer.setView(cam);

        createPauseMenu();
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

    private void createPauseMenu(){
        Skin skin = assetManager.get("ui/ui_skin.json", Skin.class);
        Viewport viewport = new FitViewport(DankDungeon.V_WIDTH, DankDungeon.V_HEIGHT, new OrthographicCamera());
        pauseMenu = new Stage(viewport, game.batch);
        Gdx.input.setInputProcessor(pauseMenu);

        Table table = new Table();
//        table.debug();
        table.sizeBy(800, 600);
        table.setPosition((DankDungeon.V_WIDTH / 2) - table.getWidth() / 2,
                (DankDungeon.V_HEIGHT / 2) - table.getHeight() / 2);

        table.background(skin.newDrawable("block", skin.getColor("dark-grey")));
        table.top();

        Label l = new Label("Pause", skin, "title");
        table.add(l).center().colspan(1).expandX().expandY();

        TextButton resumeButton = new TextButton("Resume", skin, "green-button");
        resumeButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                resumeGame();
            }
        });

        TextButton mainMenuButton = new TextButton("Exit to Menu", skin, "green-button");
        mainMenuButton.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                pauseMenu.addAction(Actions.sequence(
                        Actions.color(Color.BLACK, 0.5f),
                        new Action() {
                            @Override
                            public boolean act(float delta) {
                                dispose();
                                game.setScreen(new MainMenuScreen(game, assetManager));
                                return true;
                            }
                        }
                ));

            }
        });

        CheckBox cb = new CheckBox("BGM", skin);
        cb.setChecked(true);

        cb.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if( ((CheckBox) actor).isChecked() ){
                    bgm.play();
                }else{
                    bgm.stop();
                }
            }
        });


        table.row();
        table.add(resumeButton).colspan(1).expandY().expandX().center();
        table.row();
        table.add(mainMenuButton).colspan(1).expandY().expandX().center();
        table.row();
        table.add(cb);

        pauseMenu.addActor(table);
    }

    private void pauseGame(){
        this.state = State.PAUSED;

    }

    private void resumeGame(){
        this.state = State.RUNNING;
    }

    public void gameOver(){
        int score = hud.getScore();
        dispose();
        game.setScreen(new GameOverScreen(game, score, assetManager));
    }

    @Override
    public void render(float delta){
        if(state != State.PAUSED){
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

            cam.position.x = player.getPosition().x;
            cam.position.y = player.getPosition().y;

            // camera offset slightly towards cursor
            Vector2 pos = new Vector2();
            pos.clamp(-0.1f, 0.1f);
            pos = cursorBody.getPosition().cpy().sub(player.getPosition()).nor().scl(0.05f);
            cam.translate(pos);
            cam.update();
            hud.update(delta);
            mapRenderer.setView(cam);
        }else{
            pauseMenu.act(delta);
            if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
                resumeGame();
            }
        }

        // draw the game
        draw();

        if(state == State.PAUSED){
            pauseMenu.draw();
        }

        if(player.isSetToDestroy()){
            gameOver();
        }
    }

    private void draw(){
        Gdx.gl.glClearColor(50f / 255f, 50f / 255f, 50f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();

        if(Gdx.input.isKeyPressed(Input.Keys.NUM_1)){
            b2dr.render(world, cam.combined); // todo: remove
        }

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        hud.draw(game.batch);
        player.render(game.batch);
        for(B2DSprite sprite : entityList){
            sprite.render(game.batch);
        }
        game.batch.end();

        // set the batch to draw from the HUD's camera
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
                // create a pool of blood on the tile enemies die on
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
        if(level > 1) caveGen.destroyLevel();

        this.map = caveGen.generateCave(level);
        mapRenderer.setMap(map);
        player.redefine(CaveGenerator.cellToWorldPosition(caveGen.getHeroSpawn()));
    }

    public void levelUp(){
        levelUp = true;
    }

    private void newFloor(){
        floor++;
        // increase bgm tempo with level
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

    // TODO: remove
    public void handleInput(float dt){
//        if (Gdx.input.isKeyPressed(Input.Keys.Q)){
//            cam.zoom -= 0.1f;
//        }
//        if (Gdx.input.isKeyPressed(Input.Keys.E)){
//            cam.zoom += 0.1f;
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)){
//            levelUp();
//        }
//        if(Gdx.input.isKeyJustPressed(Input.Keys.Z)){
//            System.out.println(player.getCurrentState().toString());
//        }
//        if(Gdx.input.isKeyJustPressed(Input.Keys.T)){
//            player.damage(10000);
//        }
//        if(Gdx.input.isKeyJustPressed(Input.Keys.R)){
//            player.damage(10);
//        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            pauseGame();
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
        pauseMenu.getViewport().update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
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
