package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.models.Hero;
import com.test.test.SpaceAnts;
import com.test.test.scenes.Hud;
import com.test.test.utils.LevelDefiner;

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
    private Hud hud;

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
    private TextureRegion floorTexture;
    private TextureRegion heroTexture;

    private AssetManager assetManager;
    private LevelDefiner ld;

    public GameScreen(SpaceAnts game){
        this.game = game;
        this.world = new World(new Vector2(0, 0), true);
        this.b2dr = new Box2DDebugRenderer();
        this.cam = new OrthographicCamera();
        cam.setToOrtho(false, V_WIDTH / 2 / PPM, V_HEIGHT / 2 / PPM);
        assetManager = new AssetManager();
        this.map = new TiledMap();
        atlas = new TextureAtlas("animations/player.pack");
        this.gamePort = new FitViewport(SpaceAnts.V_WIDTH / PPM, SpaceAnts.V_HEIGHT / PPM , cam);
//        this.hud = new Hud(game.batch);
//        defineCursor();
        this.ld = new LevelDefiner(world, this);
        this.cursorBody = ld.defineCursor();
        defineMap();
        this.player = ld.defineHero(heroTexture);

        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);
        cam.position.set(gamePort.getWorldWidth() / PPM, gamePort.getWorldHeight() / 2 / PPM, 0);
        cam.zoom -= 0.6;
        mapRenderer.setView(cam);


        Texture texture = new Texture("textures/gauntness.png");
        floorTexture = new TextureRegion(texture, 112, 64, 1, 1);
        floorTexture = new TextureRegion(texture, 112, 64, 1, 1);
    }



    @Override
    public void render(float delta){
        handleInput(delta);
        stepWorld();

        player.update(delta);
        cam.position.x = player.b2body.getPosition().x;
        cam.position.y = player.b2body.getPosition().y;
        cam.update();
        mapRenderer.setView(cam);

        // openGL
        Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 100f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();
        b2dr.render(world, cam.combined);


        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        player.draw(game.batch);
        game.batch.end();
    }


    public void defineMap() {
        //        map = LevelGenerator.NewLevel()

        tiles = new Texture(Gdx.files.internal("textures/dungeon_tiles2.png"));
        int tileSize = 20;
        TextureRegion[][] splitTiles = TextureRegion.split(tiles, tileSize, tileSize);

        /* tmx */
//        map = new TmxMapLoader().load("test.tmx");
//        assetManager.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
//        assetManager.load("test.tmx", TiledMap.class);
//        assetManager.finishLoading();
//        map = assetManager.get("test.tmx");
//
//        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("floor");
//        for( int row = 0; row < layer.getHeight(); row++){
//            for(int col = 0; col <  layer.getWidth(); col++){
//                TiledMapTileLayer.Cell cell = layer.getCell(col, row);
//
//                if (cell == null) continue;
//                if (cell.getTile() == null) continue;
//                if (cell.getTile().getProperties().containsKey("type")){
//                    BodyDef bdef = new BodyDef();
//                    bdef.type = BodyDef.BodyType.StaticBody;
//                    // center of tile
//                    bdef.position.set(
//                        (col + 0.5f) * tileSize * PPM,
//                        (row + 0.5f) * tileSize * PPM
//                    );
//                    System.out.println("type");
//
//                }
//            }
//        }


        TextureRegion centreFloor = splitTiles[1][1];
        heroTexture = splitTiles[4][4];
        map = new TiledMap();
        MapLayers layers = map.getLayers();

        TiledMapTileLayer layer = new TiledMapTileLayer(16, 16, tileSize, tileSize);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(centreFloor));
                System.out.println("Y = " + y);
                layer.setCell(y, x, cell);
                if(x == 0 || y == 0 || x == 15 || y == 15){
                    Vector2 centre = new Vector2((x + 0.5f) * tileSize / 2 / PPM,
                                                 (y + 0.5f) * tileSize / 2 /PPM);
                    BodyDef bdef = new BodyDef();
                    bdef.type = BodyDef.BodyType.StaticBody;
                    bdef.position.set(centre);

                    PolygonShape wall = new PolygonShape();
                    wall.setAsBox(tileSize / 2 / PPM, tileSize / 2 / PPM, centre, 0);
                    FixtureDef fdef = new FixtureDef();
                    fdef.shape = wall;

                    world.createBody(bdef).createFixture(fdef);
                }
            }
        }

        layers.add(layer);
    }



    @Override
    public void show() {

    }

    public void handleInput(float dt){

        float MAX_VELOCITY = 1.2f;
        faceCursor();

        System.out.printf("**MOUSE: %f : %f \t", cursorBody.getPosition().x * PPM, cursorBody.getPosition().y * PPM);
        System.out.printf("**PLAYER: %f : %f \n", player.b2body.getPosition().x * PPM, player.b2body.getPosition().y * PPM);

        if(player.currentState != Hero.State.DEAD) {
            if (Gdx.input.isKeyPressed(Input.Keys.W) && player.b2body.getLinearVelocity().y <= MAX_VELOCITY) {
                player.b2body.applyLinearImpulse(new Vector2(0, 0.2f), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) && player.b2body.getLinearVelocity().x <= MAX_VELOCITY) {
                player.b2body.applyLinearImpulse(new Vector2(0.2f, 0), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A) && player.b2body.getLinearVelocity().x >= -MAX_VELOCITY) {
                player.b2body.applyLinearImpulse(new Vector2(-0.2f, 0), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S) && player.b2body.getLinearVelocity().y >= -MAX_VELOCITY) {
                player.b2body.applyLinearImpulse(new Vector2(0, -0.2f), player.b2body.getWorldCenter(), true);
            }
        }

    }

    private void faceCursor(){
        // get the game-world translation of the mouse position
        Vector3 v3 = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        float x = v3.x;
        float y = v3.y;
        float angle = MathUtils.atan2(y - player.b2body.getPosition().y, x - player.b2body.getPosition().x);

        cursorBody.setTransform(new Vector2(x, y), 0);
        player.b2body.setTransform(player.b2body.getPosition(),
                                   MathUtils.atan2(y - player.b2body.getPosition().y,
                                                   x - player.b2body.getPosition().x));
        player.sprite.setRotation(angle * MathUtils.radiansToDegrees);
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
        dispose();
    }

    @Override
    public void dispose() {
        map.dispose();
        world.dispose();
        mapRenderer.dispose();
        b2dr.dispose();
    }

    public Hud getHud(){
        return this.hud;
    }

    public TextureAtlas getAtlas(){ return this.atlas; }

    public World getWorld(){
        return this.world;
    }

    public TiledMap getMap(){
        return this.map;
    }

}
