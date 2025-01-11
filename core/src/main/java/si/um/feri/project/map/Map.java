package si.um.feri.project.map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StreamUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import si.um.feri.project.map.utils.Constants;
import si.um.feri.project.map.utils.Geolocation;
import si.um.feri.project.map.utils.MapRasterTiles;
import si.um.feri.project.map.utils.ZoomXY;
import si.um.feri.project.soccer.SoccerGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Map extends ApplicationAdapter implements GestureDetector.GestureListener {

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private Vector3 touchPosition;

    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private OrthographicCamera camera;

    private Texture[] mapTiles;
    private ZoomXY beginTile;   // top left tile

    private ArrayList<Event> events;
    private Texture stadiumTexture;
    private Event selectedEvent = null;
    private Vector2 cursorPosition;

    private SoccerGame soccerGame;

    // Center geolocation
    private final Geolocation SLOVENIA_CENTER = new Geolocation(46.557314, 15.037771);
    private final Geolocation centerGeolocation = SLOVENIA_CENTER;
    private int currentZoom = 9;
    private final float zoomAdjustment = 0.1f;

    public ArrayList<Event> fetchEvents() throws URISyntaxException, IOException {
        String urlString = "http://localhost:3000/footballMatch/filterByDateRange/2024-11-03/2024-11-10";
        String jsonString = getJsonResponse(urlString);
        return parseEvents(jsonString);
    }

    private static String getJsonResponse(String urlString) throws URISyntaxException, IOException {
        URL url = new URI(urlString).toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed : HTTP error code : " + connection.getResponseCode());
        }

        // Read response
        // Read the response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        connection.disconnect();

        return response.toString();
    }

    private ArrayList<Event> parseEvents(String jsonString) {
        ArrayList<Event> events = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Event event = new Event();
            event._id = jsonObject.getString("_id");

            try {
                event.date = dateFormat.parse(jsonObject.getString("date"));
            } catch (ParseException e) {
                Gdx.app.log("Event", "Failed to parse date: " + jsonObject.getString("date"));
            }

            event.time = jsonObject.optString("time", "");
            event.score = jsonObject.optString("score", "");
            event.location = jsonObject.optString("location", "");
            event.season = jsonObject.getInt("season");

            JSONObject homeTeam = jsonObject.getJSONObject("home");
            event.home = new Team();
            event.home._id = homeTeam.getString("_id");
            event.home.name = homeTeam.getString("name");
            event.home.logoPath = homeTeam.getString("logoPath");

            JSONObject awayTeam = jsonObject.getJSONObject("away");
            event.away = new Team();
            event.away._id = awayTeam.getString("_id");
            event.away.name = awayTeam.getString("name");
            event.away.logoPath = awayTeam.getString("logoPath");

            JSONObject stadiumObject = jsonObject.getJSONObject("stadium");
            event.stadium = new Stadium();
            event.stadium._id = stadiumObject.getString("_id");
            event.stadium.name = stadiumObject.optString("name", "");
            event.stadium.capacity = stadiumObject.getInt("capacity");
            event.stadium.buildYear = stadiumObject.getInt("buildYear");
            event.stadium.imageUrl = stadiumObject.getString("imageUrl");
            event.stadium.season = stadiumObject.getInt("season");

            JSONObject locationObject = stadiumObject.getJSONObject("location");
            JSONArray coordinates = locationObject.getJSONArray("coordinates");
            event.stadium.location = new Geolocation(coordinates.getDouble(0), coordinates.getDouble(1));

            events.add(event);
        }

        return events;
    }

    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.zoom = 1f; // Initial zoom
        camera.update();

        touchPosition = new Vector3();

        stadiumTexture = new Texture(Gdx.files.internal("map-marker.png"));

        initializeMap(currentZoom);

        GestureDetector gestureDetector = new GestureDetector(this);
        Gdx.input.setInputProcessor(new InputMultiplexer(gestureDetector, new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                camera.zoom += amountY * zoomAdjustment; // Adjust zoom speed as needed
                camera.zoom = MathUtils.clamp(camera.zoom, 0.01f, 1f); // Clamp zoom within limits
                return true;
            }
        }));

        try {
            events = fetchEvents();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeMap(int zoom) {
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(centerGeolocation.lat, centerGeolocation.lng, zoom);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES);

            beginTile = new ZoomXY(centerTile.zoom, centerTile.x - (Constants.NUM_TILES) / 2, centerTile.y - (Constants.NUM_TILES) / 2);

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
            Gdx.app.log("Map", "Failed to initialize map: " + e.getMessage());
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
        drawEventDetails();
    }

    private void drawMarkers() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (Event event : events) {
            Vector2 marker = MapRasterTiles.getPixelPosition(
                event.stadium.location.lat,
                event.stadium.location.lng,
                currentZoom,
                beginTile.x,
                beginTile.y
            );
            batch.draw(stadiumTexture, marker.x - 16, marker.y - 16, 32, 32);
        }
        batch.end();
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

        // Check if the touch position intersects with any event marker
        for (Event event : events) {
            Vector2 markerPosition = MapRasterTiles.getPixelPosition(
                event.stadium.location.lat,
                event.stadium.location.lng,
                currentZoom,
                beginTile.x,
                beginTile.y
            );

            // Marker size and touch detection (32x32)
            if (touchPosition.x >= markerPosition.x - 16 && touchPosition.x <= markerPosition.x + 16 &&
                touchPosition.y >= markerPosition.y - 16 && touchPosition.y <= markerPosition.y + 16) {
                selectedEvent = event;
                cursorPosition = new Vector2(x, y);
                return true;
            }
        }

        // Deselect event if clicked elsewhere
        selectedEvent = null;
        return false;
    }

    public void drawEventDetails() {
        if (selectedEvent == null) return;

        float x = cursorPosition.x;
        float y = cursorPosition.y + 200;

        // Load a TTF font to avoid blurriness
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/PixeloidSans-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fontParameter.size = 24;
        fontParameter.color = Color.BLACK;
        BitmapFont font = fontGenerator.generateFont(fontParameter);
        fontGenerator.dispose();

        // Load and cache team logos
        HashMap<Object, Object> logoTextures = new HashMap<>();

        logoTextures.putIfAbsent(selectedEvent.home.logoPath, fetchTextureFromUrl(selectedEvent.home.logoPath));
        logoTextures.putIfAbsent(selectedEvent.away.logoPath, fetchTextureFromUrl(selectedEvent.away.logoPath));

        Texture homeLogo = (Texture) logoTextures.get(selectedEvent.home.logoPath);
        Texture awayLogo = (Texture) logoTextures.get(selectedEvent.away.logoPath);

        // Draw the details box and content
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw the background rectangle
        shapeRenderer.setColor(new Color(0.3f, 0.5f, 1f, 0.6f));
        shapeRenderer.rect(x - 60, y - 200, 700, 460);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(x+180, y-180, 140, 60);
        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        int logoSize = 128;
        // Draw the team logos
        if (homeLogo != null) {
            batch.draw(homeLogo, x, y, logoSize, logoSize);
        }
        if (awayLogo != null) {
            batch.draw(awayLogo, x + 400, y, logoSize, logoSize);
        }

        // Draw the text
        String rawDate = selectedEvent.date.toString();
        String date = rawDate.substring(0, 10) + rawDate.substring(rawDate.length() - 5);
        font.draw(batch, selectedEvent.home.name, x + (selectedEvent.home.name.length() > 10 ? -40 : 0), y - 50);
        font.draw(batch, selectedEvent.away.name, x + 350 + (selectedEvent.away.name.length() > 10 ? -40 : selectedEvent.away.name.length() < 6 ? 80 : 40), y - 50);
        font.getData().setScale(1.5f);
        font.draw(batch, date, x + 80, y + 220);
        font.draw(batch, selectedEvent.score, x + 220, y + 80);
        font.draw(batch, "Play", x+200, y-140);
        font.getData().setScale(1f);

        batch.end();
    }

    private Texture fetchTextureFromUrl(String urlString) {
        InputStream input = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URI(urlString).toURL().openConnection();
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();

            // Read the input stream into a byte array
            byte[] bytes = StreamUtils.copyStreamToByteArray(input);

            // Create a Texture from the byte array
            return new Texture(new Pixmap(bytes, 0, bytes.length));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            StreamUtils.closeQuietly(input);
        }
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        if (button == Input.Buttons.LEFT) {
            touchPosition.set(x, y, 0);
            camera.unproject(touchPosition);
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
