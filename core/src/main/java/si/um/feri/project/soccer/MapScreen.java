package si.um.feri.project.soccer;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.json.JSONArray;
import org.json.JSONObject;

import si.um.feri.project.map.model.Event;
import si.um.feri.project.map.model.Stadium;
import si.um.feri.project.map.model.TeamRecord;
import si.um.feri.project.map.utils.Constants;
import si.um.feri.project.map.utils.Geolocation;
import si.um.feri.project.map.utils.MapRasterTiles;
import si.um.feri.project.map.utils.ZoomXY;

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
import java.util.Locale;
import java.util.Objects;

public class MapScreen extends ScreenAdapter implements GestureDetector.GestureListener {

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

    private final SoccerGame soccerGame;

    private Stage stage;
    private Window eventDetailsWindow;
    private Skin skin;
    private Table eventDetailsTable;

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
            event.home = new TeamRecord();
            event.home._id = homeTeam.getString("_id");
            event.home.name = homeTeam.getString("name");
            event.home.logoPath = homeTeam.getString("logoPath");

            JSONObject awayTeam = jsonObject.getJSONObject("away");
            event.away = new TeamRecord();
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

    public MapScreen(SoccerGame soccerGame) {
        this.soccerGame = soccerGame;
        initialize();
    }

    public void initialize() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Constants.MAP_WIDTH, Constants.MAP_HEIGHT);
        camera.position.set(Constants.MAP_WIDTH / 2f, Constants.MAP_HEIGHT / 2f, 0);
        camera.zoom = 1f; // Initial zoom
        camera.update();

        touchPosition = new Vector3();

        stadiumTexture = new Texture(Gdx.files.internal("stadiumMarker.png"));

        initializeMap(currentZoom);

        try {
            events = fetchEvents();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        stage = new Stage(new ScreenViewport());
        skin = soccerGame.getAssetManager().get(AssetDescriptors.UI_SKIN);
        createEventDetailsWindow();

        // Update input processor to include stage
        InputMultiplexer multiplexer = getInputMultiplexer();
        Gdx.input.setInputProcessor(multiplexer);
    }

    private InputMultiplexer getInputMultiplexer() {
        GestureDetector gestureDetector = new GestureDetector(this);
        return new InputMultiplexer(
            stage,
            gestureDetector,
            new InputAdapter() {
                @Override
                public boolean scrolled(float amountX, float amountY) {
                    camera.zoom += amountY * zoomAdjustment;
                    camera.zoom = MathUtils.clamp(camera.zoom, 0.01f, 1f);
                    return true;
                }
            }
        );
    }

    private void createEventDetailsWindow() {
        eventDetailsWindow = new Window("Event Details", skin);
        eventDetailsWindow.setVisible(false);
        eventDetailsWindow.setMovable(false);
        eventDetailsWindow.setModal(false);
        eventDetailsWindow.setSize(GameConfig.DETAILS_WINDOW_WIDTH, GameConfig.DETAILS_WINDOW_HEIGHT);

        eventDetailsTable = new Table(skin);
        eventDetailsTable.align(Align.center);
        eventDetailsWindow.add(eventDetailsTable).expand().fill();

        stage.addActor(eventDetailsWindow);
    }

    private void updateEventDetails(Event event) {
        if (event == null) {
            eventDetailsWindow.setVisible(false);
            return;
        }

        eventDetailsTable.clear();

        // Add match date
        String rawDate = event.date.toString();
        String date = rawDate.substring(0, 10) + rawDate.substring(rawDate.length() - 5);
        eventDetailsTable.add(new Label(date, skin, "title")).colspan(3).pad(10);
        eventDetailsTable.row();

        // Add team logos and names
        Image homeLogo = new Image(Objects.requireNonNull(fetchTextureFromUrl(event.home.logoPath)));
        Image awayLogo = new Image(Objects.requireNonNull(fetchTextureFromUrl(event.away.logoPath)));

        Table teamsTable = new Table();
        teamsTable.add(homeLogo).size(GameConfig.TEAM_LOGO_SIZE, GameConfig.TEAM_LOGO_SIZE).pad(10);
        teamsTable.add(new Label(event.score, skin, "title")).pad(20);
        teamsTable.add(awayLogo).size(GameConfig.TEAM_LOGO_SIZE, GameConfig.TEAM_LOGO_SIZE).pad(10);
        eventDetailsTable.add(teamsTable).colspan(3).pad(10);
        eventDetailsTable.row();

        Table namesTable = new Table();
        namesTable.add(new Label(event.home.name, skin, "title")).pad(10);
        namesTable.add().width(100);
        namesTable.add(new Label(event.away.name, skin, "title")).pad(10);
        eventDetailsTable.add(namesTable).colspan(3).pad(10);
        eventDetailsTable.row();

        // Add play button
        TextButton playButton = getPlayButton();
        eventDetailsTable.add(playButton).colspan(3).pad(20);

        // Position the window
        eventDetailsWindow.setPosition(
            Gdx.graphics.getWidth() / 2f - eventDetailsWindow.getWidth() / 2,
            Gdx.graphics.getHeight() / 2f - eventDetailsWindow.getHeight() / 2
        );
        eventDetailsWindow.setVisible(true);
    }

    private TextButton getPlayButton() {
        TextButton playButton = new TextButton("Play", skin, "round");
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                TextureAtlas atlas = soccerGame.getAssetManager().get(AssetDescriptors.GAMEPLAY);
                soccerGame.setScreen(new MenuScreen(soccerGame,
                    new Team(selectedEvent.home.name, atlas.findRegion(selectedEvent.home.name.split(" ")[0].toLowerCase()), atlas.findRegion(selectedEvent.home.name.split(" ")[0].toLowerCase() + "p")),
                    new Team(selectedEvent.away.name, atlas.findRegion(selectedEvent.away.name.split(" ")[0].toLowerCase()), atlas.findRegion(selectedEvent.away.name.split(" ")[0].toLowerCase() + "p")),
                    Mode.SINGLEPLAYER));
            }
        });
        return playButton;
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
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        handleInput();

        camera.update();

        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();

        drawMarkers();

        // Update and render stage
        stage.act(delta);
        stage.draw();
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
            batch.draw(stadiumTexture, marker.x - GameConfig.STADUIM_SIZE / 2, marker.y - GameConfig.STADUIM_SIZE / 2, GameConfig.STADUIM_SIZE, GameConfig.STADUIM_SIZE);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        for (Texture tile : mapTiles) {
            tile.dispose();
        }
        if (stadiumTexture != null) {
            stadiumTexture.dispose();
        }
        stage.dispose();
        skin.dispose();
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

            if (touchPosition.x >= markerPosition.x - 24 && touchPosition.x <= markerPosition.x + 24 &&
                touchPosition.y >= markerPosition.y - 16 && touchPosition.y <= markerPosition.y + 16) {
                selectedEvent = event;
                updateEventDetails(event);
                return true;
            }
        }

        // Deselect event if clicked elsewhere
        selectedEvent = null;
        updateEventDetails(null);
        return false;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
            Gdx.app.log("Team Image", "Failed to fetch image: " + e.getMessage());
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
