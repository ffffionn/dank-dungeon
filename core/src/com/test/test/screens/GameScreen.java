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
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
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

    private Texture tiles;
    private TextureRegion floorTexture;


    public GameScreen(SpaceAnts game){
        this.game = game;
        this.world = new World(new Vector2(0, 0), true);
        this.b2dr = new Box2DDebugRenderer();
        this.cam = new OrthographicCamera();
        this.map = new TiledMap();
        this.gamePort = new FitViewport(SpaceAnts.V_WIDTH, SpaceAnts.V_HEIGHT, cam);
//        this.hud = new Hud(game.batch);
        this.player = new Hero(this);
        defineMap();
        this.mapRenderer = new OrthogonalTiledMapRenderer(map);
        cam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);
        mapRenderer.setView(cam);


        Texture texture = new Texture("textures/gauntness.png");
        floorTexture = new TextureRegion(texture, 112, 64, 1, 1);
    }



    @Override
    public void render(float delta){
        handleInput(delta);
        world.step(1/60f, 6, 2);

        player.update(delta);
        cam.position.x = player.b2body.getPosition().x;
        cam.position.y = player.b2body.getPosition().y;

        // openGL
        Gdx.gl.glClearColor(0,0,0,1);
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


    public void defineMap(){
        //        map = LevelGenerator.NewLevel()

        tiles = new Texture(Gdx.files.internal("textures/gauntness.png"));
        TextureRegion[][] splitTiles = TextureRegion.split(tiles, 16, 16);

        System.out.println(splitTiles.length);

        map = new TiledMap();
        MapLayers layers = map.getLayers();
//        for (int l = 0; l < 20; l++) {
            TiledMapTileLayer layer = new TiledMapTileLayer(115, 160, 16, 16);
            for (int x = 0; x < splitTiles.length; x++) {
                for (int y = 0; y < splitTiles[x].length; y++) {
                    int ty = (int)(Math.random() * splitTiles.length);
                    int tx = (int)(Math.random() * splitTiles[ty].length);
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(new StaticTiledMapTile(splitTiles[x][y]));
                    layer.setCell(x, y, cell);
                }
            }
            layers.add(layer);
//        }



//
//        BodyDef bdef = new BodyDef();
//        PolygonShape shape = new PolygonShape();
//        FixtureDef fdef = new FixtureDef();
//        Body body;
//
//        for(MapObject object : map.getLayers().get(0).getObjects().getByType(RectangleMapObject.class)){
//            System.out.println("loop");
//            Rectangle rect = ((RectangleMapObject) object).getRectangle();
//
//            bdef.type = BodyDef.BodyType.StaticBody;
//            bdef.position.set(rect.getX() + rect.getWidth() / 2,
//                              rect.getY() + rect.getHeight() / 2);
//
//            body = world.createBody(bdef);
//
//            shape.setAsBox(rect.getWidth() / 2, rect.getHeight() / 2);
//            fdef.shape = shape;
//            body.createFixture(fdef);
//        }
//
//        System.out.println(map.getLayers().get(0));
//        System.out.println(map);
    }

    public void defineHero(){

    }

    @Override
    public void show() {

    }

    public void handleInput(float dt){
        //control our player using immediate impulses
        if(player.currentState != Hero.State.DEAD) {
            if (Gdx.input.isKeyPressed(Input.Keys.UP)){
                player.b2body.applyLinearImpulse(new Vector2(0, 2f), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                player.b2body.applyLinearImpulse(new Vector2(2f, 0), player.b2body.getWorldCenter(), true);
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                player.b2body.applyLinearImpulse(new Vector2(-2f, 0), player.b2body.getWorldCenter(), true);
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                player.b2body.applyLinearImpulse(new Vector2(0, -2f), player.b2body.getWorldCenter(), true);
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
        this.world.dispose();
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
