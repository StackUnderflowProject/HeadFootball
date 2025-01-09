package si.um.feri.project.map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import si.um.feri.project.map.utils.Constants;
import si.um.feri.project.map.utils.Geolocation;
import si.um.feri.project.map.utils.MapRasterTiles;
import si.um.feri.project.map.utils.ZoomXY;

import java.io.IOException;

public class Map extends ApplicationAdapter implements GestureDetector.GestureListener {

    private ShapeRenderer shapeRenderer;
    private Vector3 touchPosition;

    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    private Texture[] mapTiles;
    private ZoomXY beginTile;   // top left tile

    private Array<Vector2> markers;

    private Geolocation[][] ljToMb;

    // Center geolocation
    private final Geolocation SLOVENIA_CENTER = new Geolocation(46.557314, 15.037771);
    private final Geolocation centerGeolocation = SLOVENIA_CENTER;
    private int currentZoom = 9;
    private final float zoomAdjustment = 0.1f;

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.zoom = 1f; // Initial zoom
        camera.update();

        touchPosition = new Vector3();

        markers = new Array<>();

        initializeMap(currentZoom);

        ljToMb = MapRasterTiles.fetchPath(new Geolocation[]{});

        GestureDetector gestureDetector = new GestureDetector(this);
        Gdx.input.setInputProcessor(new InputMultiplexer(gestureDetector, new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom += amountY * zoomAdjustment; // Adjust zoom speed as needed
                camera.zoom = MathUtils.clamp(camera.zoom, 0.01f, 1f); // Clamp zoom within limits
                return true;
            }
        }));
    }

    private void initializeMap(int zoom) {
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(centerGeolocation.lat, centerGeolocation.lng, zoom);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);

            beginTile = new ZoomXY(centerTile.zoom, centerTile.x - (Constants.NUM_TILES - 1) / 2, centerTile.y - (Constants.NUM_TILES - 1) / 2);

            // Create and populate TiledMap
            if (tiledMap == null) {
                tiledMap = new TiledMap();
            } else {
                for (MapLayer layer : tiledMap.getLayers()) {
                    tiledMap.getLayers().remove(layer);
                }
            }

            MapLayers layers = tiledMap.getLayers();
            TiledMapTileLayer layer = new TiledMapTileLayer(Constants.NUM_TILES, Constants.NUM_TILES, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);

            int index = 0;
            for (int j = Constants.NUM_TILES - 1; j >= 0; j--) {
                for (int i = 0; i < Constants.NUM_TILES; i++) {
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                    layer.setCell(i, j, cell);
                    index++;
                }
            }
            layers.add(layer);

            tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void clampCameraWithinBounds() {
        float minX = 0, maxX = MapRasterTiles.TILE_SIZE * Constants.NUM_TILES;
        float minY = 0, maxY = MapRasterTiles.TILE_SIZE * Constants.NUM_TILES;
        float viewportWidth = camera.viewportWidth * camera.zoom;
        float viewportHeight = camera.viewportHeight * camera.zoom;

        camera.position.x = MathUtils.clamp(camera.position.x, minX + viewportWidth / 2f, maxX - viewportWidth / 2f);
        camera.position.y = MathUtils.clamp(camera.position.y, minY + viewportHeight / 2f, maxY - viewportHeight / 2f);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        drawMarkers();
    }

    private void drawMarkers() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Vector2 marker : markers) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.circle(marker.x, marker.y, 10);
        }

        for (Geolocation[] g: ljToMb){
            for(Geolocation geolocation: g){
                ZoomXY tile = MapRasterTiles.getTileNumber(geolocation.lat, geolocation.lng, currentZoom);
                float x = (tile.x - beginTile.x) * MapRasterTiles.TILE_SIZE + MapRasterTiles.TILE_SIZE / 2;
                float y = (Constants.NUM_TILES - 1 - (tile.y - beginTile.y)) * MapRasterTiles.TILE_SIZE + MapRasterTiles.TILE_SIZE / 2;
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.circle(x, y, 10);
            }
        }

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        for (Texture tile : mapTiles) {
            tile.dispose();
        }
    }

    private void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            camera.zoom += zoomAdjustment;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom -= zoomAdjustment;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            camera.translate(-3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            camera.translate(3, 0, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            camera.translate(0, -3, 0);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            camera.translate(0, 3, 0);
        }

        camera.zoom = MathUtils.clamp(camera.zoom, 0.01f, 1f);

        float newZoomLevel = calculateZoomLevel(camera.zoom);
        if (newZoomLevel != currentZoom) {
            currentZoom = (int) newZoomLevel;
            initializeMap(currentZoom); // Reload tiles for the new zoom level
        }

        clampCameraWithinBounds();
    }


    private int calculateZoomLevel(float zoom) {
        if (zoom < 0.5f) return 9;
        return 8;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (button == Input.Buttons.LEFT) {
            touchPosition.set(x, y, 0);
            camera.unproject(touchPosition);
            markers.add(new Vector2(touchPosition.x, touchPosition.y));
            return true;
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        camera.translate(-deltaX, deltaY);
        return true;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        camera.zoom += ((initialDistance > distance) ? zoomAdjustment : -zoomAdjustment);
        return true;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
    }
}
