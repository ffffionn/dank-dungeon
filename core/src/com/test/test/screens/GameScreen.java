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
import com.test.test.models.Fireball;
import com.test.test.models.Hero;
import com.test.test.SpaceAnts;
import com.test.test.scenes.Hud;
import com.test.test.utils.LevelDefiner;
import javafx.scene.input.MouseButton;

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



    private AssetManager assetManager;
    private LevelDefiner ld;

    public GameScreen(SpaceAnts game){
        this.game = game;
        this.world = new World(new Vector2(0, 0), true);
        this.b2dr = new Box2DDebugRenderer();
        this.cam = new OrthographicCamera();
        cam.setToOrtho(false, V_WIDTH / 2 / PPM, V_HEIGHT / 2 / PPM);
        this.assetManager = new AssetManager();
        this.map = new TiledMap();
        this.atlas = new TextureAtlas("animations/player.pack");
        this.gamePort = new FitViewport(SpaceAnts.V_WIDTH / PPM, SpaceAnts.V_HEIGHT / PPM , cam);
//        this.hud = new Hud(game.batch);
        this.ld = new LevelDefiner(world, this);
        this.cursorBody = ld.defineCursor();
        this.tiles = new Texture(Gdx.files.internal("textures/dungeon_tiles2.png"));
        this.player = ld.defineHero();
        ld.defineMap(tiles);

        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / PPM);
        cam.position.set(gamePort.getWorldWidth() / PPM, gamePort.getWorldHeight() / 2 / PPM, 0);
        cam.zoom -= 0.6;
        mapRenderer.setView(cam);

        //set Player animation frams
        TextureAtlas.AtlasRegion region = atlas.findRegion("player-move");
        TextureRegion[] moveFrames = region.split(64, 64)[0];
        player.setTexture(moveFrames[0]);
        player.setAnimation(moveFrames, 1 / 12f);
    }

    @Override
    public void render(float delta){
        handleInput(delta);
        stepWorld();

        player.update(delta);
        cam.position.x = player.getPosition().x;
        cam.position.y = player.getPosition().y;
        cam.update();
        mapRenderer.setView(cam);

        // openGL
        Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 100f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.render();
        b2dr.render(world, cam.combined);

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        player.render(game.batch);
        game.batch.end();
    }

    @Override
    public void show() {

    }

    public void handleInput(float dt){

        float MAX_VELOCITY = 2.0f;
        faceCursor();
//
//        System.out.printf("**MOUSE: %f : %f \t", cursorBody.getPosition().x * PPM, cursorBody.getPosition().y * PPM);
//        System.out.printf("**PLAYER: %f : %f \n", player.getBody().getPosition().x * PPM, player.getBody().getPosition().y * PPM);

        if(player.currentState != Hero.State.DEAD) {
            if (Gdx.input.isKeyPressed(Input.Keys.W) && player.getBody().getLinearVelocity().y <= MAX_VELOCITY) {
                player.getBody().applyLinearImpulse(new Vector2(0, 0.2f), player.getBody().getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D) && player.getBody().getLinearVelocity().x <= MAX_VELOCITY) {
                player.getBody().applyLinearImpulse(new Vector2(0.2f, 0), player.getBody().getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.A) && player.getBody().getLinearVelocity().x >= -MAX_VELOCITY) {
                player.getBody().applyLinearImpulse(new Vector2(-0.2f, 0), player.getBody().getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S) && player.getBody().getLinearVelocity().y >= -MAX_VELOCITY) {
                player.getBody().applyLinearImpulse(new Vector2(0, -0.2f), player.getBody().getWorldCenter(), true);
            }
            if (Gdx.input.justTouched()) player.shoot();
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

    public Body getCursor() { return this.cursorBody; }

}
