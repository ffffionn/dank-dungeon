package com.test.test.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.test.test.models.Hero;
import com.test.test.SpaceAnts;
import com.test.test.scenes.Hud;

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


    public GameScreen(SpaceAnts game){
        this.game = game;
        this.world = new World(new Vector2(0, 0), true);
        this.b2dr = new Box2DDebugRenderer();
        this.cam = new OrthographicCamera();
        cam.setToOrtho(false, 10, 20);
        this.map = new TiledMap();
        this.gamePort = new FitViewport(SpaceAnts.V_WIDTH / SpaceAnts.PPM, SpaceAnts.V_HEIGHT / SpaceAnts.PPM , cam);
//        this.hud = new Hud(game.batch);
        this.player = new Hero(this, heroTexture);
        defineCursor();
        defineMap();
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / SpaceAnts.PPM);
        cam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);
        cam.zoom -= 0.3;
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

        // openGL
        Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 250f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        cam.update();
        mapRenderer.setView(cam);
        mapRenderer.render();
        b2dr.render(world, cam.combined);

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
//        player.draw(game.batch);
        game.batch.end();
    }


    public void defineMap() {
        //        map = LevelGenerator.NewLevel()

        tiles = new Texture(Gdx.files.internal("textures/dungeon_tiles2.png"));
        TextureRegion[][] splitTiles = TextureRegion.split(tiles, 20, 20);

        TextureRegion centreFloor = splitTiles[1][1];

        System.out.println(splitTiles.length);

        map = new TiledMap();
        MapLayers layers = map.getLayers();

        // layer 0
        TiledMapTileLayer layer = new TiledMapTileLayer(240, 240, 20, 20);
        for (int x = 0; x < layer.getWidth(); x++) {
            for (int y = 0; y < layer.getHeight(); y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(centreFloor));
                System.out.println("Y = " + y);
                layer.setCell(y, x, cell);
            }
            System.out.println("X = " + x);
        }

        layers.add(layer);


        // layer 1
        layer = new TiledMapTileLayer(240, 240, 20, 20);
        for (int x = 0; x < splitTiles.length; x++) {
            for (int y = 0; y < splitTiles[x].length; y++) {
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(new StaticTiledMapTile(splitTiles[x][y]));
                System.out.println("Y = " + y);
//                        layer.setCell(y, x, cell.setRotation(TiledMapTileLayer.Cell.ROTATE_90));
                layer.setCell(y, x, cell);
            }
            System.out.println("X = " + x);
        }

//        layers.add(layer);


    }


    public void defineCursor(){
        BodyDef bdef = new BodyDef();
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.position.set(32 / SpaceAnts.PPM, 32 / SpaceAnts.PPM);

        this.cursorBody = world.createBody(bdef);


        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(5 / SpaceAnts.PPM);
        fdef.shape = shape;
        fdef.density = 0f;
        fdef.friction = 0f;
        fdef.restitution = 0f;

        cursorBody.createFixture(fdef);

        WheelJointDef jointDef = new WheelJointDef();
        jointDef.maxMotorTorque = 0f;
        jointDef.frequencyHz = 0f;
        jointDef.motorSpeed = 0f;
        jointDef.dampingRatio = 0f;
        jointDef.initialize(player.b2body, cursorBody, player.b2body.getPosition(), new Vector2(1, 1));

        WheelJoint joint = (WheelJoint) world.createJoint(jointDef);
        joint.setMotorSpeed(0f);

    }

    @Override
    public void show() {

    }

    public void handleInput(float dt){
        //control our player using immediate impulses

        float x = (float) Gdx.input.getX();
        float y = (float) Gdx.input.getY();
//        float h = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

        float angle = player.b2body.getPosition().angle(new Vector2(x, y));
//
        player.b2body.setTransform(player.b2body.getPosition(), MathUtils.degreesToRadians * angle);
//        cursorBody.setTransform(new Vector2(x, y), 0);

//        System.out.printf("X: %f   Y: %f   Angle: %f  \n", x, y, MathUtils.degreesToRadians * angle);
        System.out.printf("X: %f   Y: %f   Angle: %f  \n", player.b2body.getPosition().x, player.b2body.getPosition().y, MathUtils.degreesToRadians * angle);
//        System.out.printf("X: %f   Y: %f   Angle: %f  \n", cursorBody.getPosition().x, cursorBody.getPosition().y, MathUtils.degreesToRadians * angle);

        if(player.currentState != Hero.State.DEAD) {
            if (Gdx.input.isKeyPressed(Input.Keys.W) && player.b2body.getLinearVelocity().y <= 1) {
                player.b2body.applyLinearImpulse(new Vector2(0, 0.2f), player.b2body.getWorldCenter(), true);
//                player.b2body.setTransform(player.b2body.getPosition(), 1.5f);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) && player.b2body.getLinearVelocity().x <= 1) {
                player.b2body.applyLinearImpulse(new Vector2(0.2f, 0), player.b2body.getWorldCenter(), true);
//                player.b2body.setTransform(player.b2body.getPosition(), 0.0f);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A) && player.b2body.getLinearVelocity().x >= -1) {
//                player.b2body.setTransform(player.b2body.getPosition(), 3.1f);
                player.b2body.applyLinearImpulse(new Vector2(-0.2f, 0), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S) && player.b2body.getLinearVelocity().y >= -1) {
                player.b2body.applyLinearImpulse(new Vector2(0, -0.2f), player.b2body.getWorldCenter(), true);
//                player.b2body.setTransform(player.b2body.getPosition(), -1.5f);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
                player.b2body.applyTorque(2f, true);
            }
        }
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

    public World getWorld(){
        return this.world;
    }

    public TiledMap getMap(){
        return this.map;
    }

}
