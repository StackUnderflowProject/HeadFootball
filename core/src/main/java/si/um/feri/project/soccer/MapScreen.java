package si.um.feri.project.soccer;

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
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import org.json.JSONArray;
import org.json.JSONObject;

import si.um.feri.project.map.model.Event;
import si.um.feri.project.map.model.Host;
import si.um.feri.project.map.model.Match;
import si.um.feri.project.map.model.Stadium;
import si.um.feri.project.map.model.TeamRecord;
import si.um.feri.project.map.utils.Constants;
import si.um.feri.project.map.utils.Geolocation;
import si.um.feri.project.map.utils.MapRasterTiles;
import si.um.feri.project.map.utils.MapTileCache;
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
import java.time.LocalDate;
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

    private ArrayList<Match> footballMatches;
    private ArrayList<Match> handballMatches; // New list for handball matches
    private ArrayList<Event> userEvents;
    private boolean showUserEvents = true;
    private Texture eventMarkerTexture;
    private boolean showFootball = true; // Filter flags
    private boolean showHandball = true;
    private Texture stadiumTexture;
    private Texture arenaTexture; // New texture for handball arenas
    private Match selectedFootballMatch = null;

    private final SoccerGame soccerGame;

    private Stage stage;
    private Window eventDetailsWindow;
    private Skin skin;
    private Table eventDetailsTable;

    // Center geolocation
    private final Geolocation SLOVENIA_CENTER = new Geolocation(46.127314, 15.037771);
    private final Geolocation centerGeolocation = SLOVENIA_CENTER;
    private int currentZoom = 9;
    private final float zoomAdjustment = 0.1f;

    private LocalDate startDate = LocalDate.now();
    private LocalDate endDate = LocalDate.now().plusDays(1);
    private Label startDateLabel;
    private Label endDateLabel;

    private void createDateRangeWindow() {
        Window dateRangeWindow = new Window("Date Range", skin);
        dateRangeWindow.setMovable(true);
        dateRangeWindow.setModal(false);
        dateRangeWindow.setSize(350, 240);
        dateRangeWindow.setPosition(
            Gdx.graphics.getWidth() - 310,
            Gdx.graphics.getHeight() - 210
        );

        // Table to hold the main content
        Table dateTable = new Table(skin);
        dateTable.align(Align.center);

        // Start Date Row
        dateTable.add(new Label("Start Date: ", skin)).pad(5);
        startDateLabel = new Label(startDate.toString(), skin);
        dateTable.add(startDateLabel).pad(5);
        TextButton startDateButton = new TextButton("Select", skin, "round");
        startDateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showDatePicker(true);
            }
        });
        dateTable.add(startDateButton).pad(5);
        dateTable.row();

        // End Date Row
        dateTable.add(new Label("End Date: ", skin)).pad(5);
        endDateLabel = new Label(endDate.toString(), skin);
        dateTable.add(endDateLabel).pad(5);
        TextButton endDateButton = new TextButton("Select", skin, "round");
        endDateButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showDatePicker(false);
            }
        });
        dateTable.add(endDateButton).pad(5);
        dateTable.row();

        // Apply Button
        TextButton applyButton = new TextButton("Apply", skin, "round");
        applyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshMatches();
            }
        });
        dateTable.add(applyButton).colspan(3).pad(20);

        // Add the main content table
        dateRangeWindow.add(dateTable).expand().fill();

        // Minimize button in the title bar
        ImageButton minimizeButton = new ImageButton(new TextureRegionDrawable(new Texture(Gdx.files.internal("1.png"))));
        minimizeButton.setSize(20, 20); // Explicitly set the size
        minimizeButton.addListener(new ChangeListener() {
            private boolean isMinimized = false;

            @Override
            public void changed(ChangeEvent event, Actor actor) {
                isMinimized = !isMinimized;
                if (isMinimized) {
                    dateTable.setVisible(false);
                    dateRangeWindow.setHeight(40); // Height of the title bar
                } else {
                    dateTable.setVisible(true);
                    dateRangeWindow.setHeight(240); // Original window height
                }
            }
        });

        // Add the minimize button to the window's title bar
        dateRangeWindow.getTitleTable().add(minimizeButton).padLeft(10);

        // Add the window to the stage
        stage.addActor(dateRangeWindow);
    }

    private void showDatePicker(final boolean isStartDate) {
        final Window datePickerWindow = new Window("Select Date", skin);
        datePickerWindow.setModal(true);
        datePickerWindow.setMovable(true);
        datePickerWindow.setSize(300, 300);
        datePickerWindow.setPosition(
            Gdx.graphics.getWidth() / 2f - 150,
            Gdx.graphics.getHeight() / 2f - 200
        );

        Table pickerTable = new Table(skin);

        // Year selection (2024-2025)
        final SelectBox<Integer> yearSelect = new SelectBox<>(skin);
        yearSelect.setItems(2024, 2025);
        yearSelect.setSelected(isStartDate ? startDate.getYear() : endDate.getYear());

        // Month selection (1-12)
        final SelectBox<Integer> monthSelect = new SelectBox<>(skin);
        monthSelect.setItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        monthSelect.setSelected(isStartDate ? startDate.getMonthValue() : endDate.getMonthValue());

        // Day selection (1-31)
        final SelectBox<Integer> daySelect = new SelectBox<>(skin);
        updateDays(yearSelect.getSelected(), monthSelect.getSelected(), daySelect);
        daySelect.setSelected(isStartDate ? startDate.getDayOfMonth() : endDate.getDayOfMonth());

        // Add listeners to update days when month/year changes
        monthSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateDays(yearSelect.getSelected(), monthSelect.getSelected(), daySelect);
            }
        });

        yearSelect.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateDays(yearSelect.getSelected(), monthSelect.getSelected(), daySelect);
            }
        });

        pickerTable.add(new Label("Year: ", skin)).pad(5);
        pickerTable.add(yearSelect).pad(5);
        pickerTable.row();
        pickerTable.add(new Label("Month: ", skin)).pad(5);
        pickerTable.add(monthSelect).pad(5);
        pickerTable.row();
        pickerTable.add(new Label("Day: ", skin)).pad(5);
        pickerTable.add(daySelect).pad(5);
        pickerTable.row();

        // OK Button
        TextButton okButton = new TextButton("OK", skin, "round");
        okButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedDate = String.format(Locale.US, "%d-%02d-%02d",
                    yearSelect.getSelected(),
                    monthSelect.getSelected(),
                    daySelect.getSelected());


                if (isStartDate) {
                    startDate = LocalDate.parse(selectedDate);
                    startDateLabel.setText(startDate.toString());
                } else {
                    endDate = LocalDate.parse(selectedDate);
                    endDateLabel.setText(endDate.toString());
                }
                datePickerWindow.remove();
            }
        });
        pickerTable.add(okButton).colspan(2).pad(20);

        datePickerWindow.add(pickerTable).expand().fill();
        stage.addActor(datePickerWindow);
    }

    private void updateDays(int year, int month, SelectBox<Integer> daySelect) {
        int maxDays;
        switch (month) {
            case 2: // February
                maxDays = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                maxDays = 30;
                break;
            default:
                maxDays = 31;
        }

        Array<Integer> days = new Array<>();
        for (int i = 1; i <= maxDays; i++) {
            days.add(i);
        }
        daySelect.setItems(days);
    }

    private void refreshMatches() {
        try {
            footballMatches = fetchEvents("footballMatch");
            handballMatches = fetchEvents("handballMatch");
        } catch (URISyntaxException | IOException e) {
            Gdx.app.error("Matches", "Failed to refresh matches: " + e.getMessage());
        }
    }

    public ArrayList<Match> fetchEvents(String sportType) throws URISyntaxException, IOException {
        String urlString = "http://localhost:3000/" + sportType + "/filterByDateRange/" + startDate + "/" + endDate;
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

    private ArrayList<Match> parseEvents(String jsonString) {
        ArrayList<Match> matches = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Match match = new Match();
            match._id = jsonObject.getString("_id");

            try {
                match.date = dateFormat.parse(jsonObject.getString("date"));
            } catch (ParseException e) {
                Gdx.app.log("Event", "Failed to parse date: " + jsonObject.getString("date"));
            }

            match.time = jsonObject.optString("time", "");
            match.score = jsonObject.optString("score", "");
            match.location = jsonObject.optString("location", "");
//            match.season = jsonObject.getInt("season");

            JSONObject homeTeam = jsonObject.getJSONObject("home");
            match.home = new TeamRecord();
            match.home._id = homeTeam.getString("_id");
            match.home.name = homeTeam.getString("name");
            match.home.logoPath = homeTeam.getString("logoPath");

            JSONObject awayTeam = jsonObject.getJSONObject("away");
            match.away = new TeamRecord();
            match.away._id = awayTeam.getString("_id");
            match.away.name = awayTeam.getString("name");
            match.away.logoPath = awayTeam.getString("logoPath");

            JSONObject stadiumObject = jsonObject.getJSONObject("stadium");
            match.stadium = new Stadium();
            match.stadium._id = stadiumObject.getString("_id");
            match.stadium.name = stadiumObject.optString("name", "");
//            match.stadium.season = stadiumObject.getInt("season");

            JSONObject locationObject = stadiumObject.getJSONObject("location");
            JSONArray coordinates = locationObject.getJSONArray("coordinates");
            match.stadium.location = new Geolocation(coordinates.getDouble(0), coordinates.getDouble(1));

            matches.add(match);
        }

        return matches;
    }

    private ArrayList<Event> fetchUserEvents() throws URISyntaxException, IOException {
        String urlString = "http://localhost:3000/events/upcoming";
        String jsonString = getJsonResponse(urlString);
        return parseUserEvents(jsonString);
    }

    private ArrayList<Event> parseUserEvents(String jsonString) {
        ArrayList<Event> events = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(jsonString);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            Event event = new Event();
            event._id = jsonObject.getString("_id");
            event.name = jsonObject.getString("name");
            event.description = jsonObject.getString("description");
            event.activity = jsonObject.getString("activity");
            event.date = jsonObject.getString("date");
            event.time = jsonObject.getString("time");
//            event.image = jsonObject.getString("image") != null ? jsonObject.getString("image") : "";
            event.predicted_count = jsonObject.getInt("predicted_count");

            // Parse host
            JSONObject hostObj = jsonObject.getJSONObject("host");
            Host host = new Host();
            host._id = hostObj.getString("_id");
            host.username = hostObj.getString("username");
            host.email = hostObj.getString("email");
            event.host = host;

            // Parse location
            JSONObject locationObj = jsonObject.getJSONObject("location");
            JSONArray coordinates = locationObj.getJSONArray("coordinates");
            event.location = new Geolocation(coordinates.getDouble(1), coordinates.getDouble(0));

            // Parse followers
            JSONArray followersArray = jsonObject.getJSONArray("followers");
            event.followers = new ArrayList<>();
            for (int j = 0; j < followersArray.length(); j++) {
                event.followers.add(followersArray.getString(j));
            }

            events.add(event);
        }

        System.out.println(events.size());

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
        camera.zoom = 0.9f; // Initial zoom
        camera.update();

        touchPosition = new Vector3();

        stadiumTexture = new Texture(Gdx.files.internal("stadiumMarker.png"));
        arenaTexture = new Texture(Gdx.files.internal("arenaMarker.png")); // Load new texture for handball arenas
        eventMarkerTexture = new Texture(Gdx.files.internal("eventMarker.png"));

        initializeMap(currentZoom);

        try {
            footballMatches = fetchEvents("footballMatch");
            handballMatches = fetchEvents("handballMatch");
            userEvents = fetchUserEvents();
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }

        stage = new Stage(new ScreenViewport());
        skin = soccerGame.getAssetManager().get(AssetDescriptors.UI_SKIN);
        createEventDetailsWindow();
        createFilterWindow();
        createDateRangeWindow();

        // Update input processor to include stage
        InputMultiplexer multiplexer = getInputMultiplexer();
        Gdx.input.setInputProcessor(multiplexer);
    }

    // Modify createFilterWindow() to add user events filter
    private void createFilterWindow() {
        Window filterWindow = new Window("Filters", skin);
        filterWindow.setMovable(true);
        filterWindow.setModal(false);
        filterWindow.setSize(200, 180); // Increased height for new checkbox
        filterWindow.setPosition(10, Gdx.graphics.getHeight() - 190);

        Table filterTable = new Table(skin);
        filterTable.align(Align.center);

        final CheckBox footballCheckbox = new CheckBox(" Football Matches", skin);
        final CheckBox handballCheckbox = new CheckBox(" Handball Matches", skin);
        final CheckBox userEventsCheckbox = new CheckBox(" User Events", skin);

        footballCheckbox.setChecked(showFootball);
        handballCheckbox.setChecked(showHandball);
        userEventsCheckbox.setChecked(showUserEvents);

        footballCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showFootball = footballCheckbox.isChecked();
            }
        });

        handballCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showHandball = handballCheckbox.isChecked();
            }
        });

        userEventsCheckbox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showUserEvents = userEventsCheckbox.isChecked();
            }
        });

        filterTable.add(footballCheckbox).pad(10).row();
        filterTable.add(handballCheckbox).pad(10).row();
        filterTable.add(userEventsCheckbox).pad(10).row();

        filterWindow.add(filterTable).expand().fill();
        stage.addActor(filterWindow);
    }

    private void drawMarkers() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw football markers
        if (showFootball) {
            for (Match match : footballMatches) {
                Vector2 marker = MapRasterTiles.getPixelPosition(
                    match.stadium.location.lat,
                    match.stadium.location.lng,
                    currentZoom,
                    beginTile.x,
                    beginTile.y
                );
                batch.draw(stadiumTexture,
                    marker.x - GameConfig.STADUIM_SIZE / 2,
                    marker.y - GameConfig.STADUIM_SIZE / 2,
                    GameConfig.STADUIM_SIZE,
                    GameConfig.STADUIM_SIZE
                );
            }
        }

        // Draw handball markers
        if (showHandball) {
            for (Match match : handballMatches) {
                Vector2 marker = MapRasterTiles.getPixelPosition(
                    match.stadium.location.lat,
                    match.stadium.location.lng,
                    currentZoom,
                    beginTile.x,
                    beginTile.y
                );
                batch.draw(arenaTexture,
                    marker.x - GameConfig.STADUIM_SIZE / 2,
                    marker.y - GameConfig.STADUIM_SIZE / 2,
                    GameConfig.STADUIM_SIZE,
                    GameConfig.STADUIM_SIZE
                );
            }
        }

        if (showUserEvents && userEvents != null) {
            for (Event event : userEvents) {
                Vector2 marker = MapRasterTiles.getPixelPosition(
                    event.location.lat,
                    event.location.lng,
                    currentZoom,
                    beginTile.x,
                    beginTile.y
                );
                batch.draw(eventMarkerTexture,
                    marker.x - GameConfig.STADUIM_SIZE / 2,
                    marker.y - GameConfig.STADUIM_SIZE / 2,
                    GameConfig.STADUIM_SIZE,
                    GameConfig.STADUIM_SIZE
                );
            }
        }

        batch.end();
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

    private void updateEventDetails(Match match) {
        if (match == null) {
            eventDetailsWindow.setVisible(false);
            return;
        }

        eventDetailsTable.clear();

        // Add match date
        String rawDate = match.date.toString();
        String date = rawDate.substring(0, 10) + rawDate.substring(rawDate.length() - 5);
        eventDetailsTable.add(new Label(date, skin, "title")).colspan(3).pad(10);
        eventDetailsTable.row();

        // Add team logos and names
        Image homeLogo = new Image(Objects.requireNonNull(fetchTextureFromUrl(match.home.logoPath)));
        Image awayLogo = new Image(Objects.requireNonNull(fetchTextureFromUrl(match.away.logoPath)));

        Table teamsTable = new Table();
        teamsTable.add(homeLogo).size(GameConfig.TEAM_LOGO_SIZE, GameConfig.TEAM_LOGO_SIZE).pad(10);
        teamsTable.add(new Label(match.score, skin, "title")).pad(20);
        teamsTable.add(awayLogo).size(GameConfig.TEAM_LOGO_SIZE, GameConfig.TEAM_LOGO_SIZE).pad(10);
        eventDetailsTable.add(teamsTable).colspan(3).pad(10);
        eventDetailsTable.row();

        Table namesTable = new Table();
        namesTable.add(new Label(match.home.name, skin, "title")).pad(10);
        namesTable.add().width(100);
        namesTable.add(new Label(match.away.name, skin, "title")).pad(10);
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

    // Add method to update event details window for user events
    private void updateUserEventDetails(Event event) {
        if (event == null) {
            eventDetailsWindow.setVisible(false);
            return;
        }

        eventDetailsTable.clear();

        // Event name
        eventDetailsTable.add(new Label(event.name, skin, "title")).colspan(3).pad(10);
        eventDetailsTable.row();

        // Event details
        Table detailsTable = new Table();
        detailsTable.add(new Label("Host: " + event.host.username, skin)).pad(5).row();
        detailsTable.add(new Label("Date: " + event.date, skin)).pad(5).row();
        detailsTable.add(new Label("Time: " + event.time, skin)).pad(5).row();
        detailsTable.add(new Label("Activity: " + event.activity, skin)).pad(5).row();
        detailsTable.add(new Label("Expected Attendees: " + event.predicted_count, skin)).pad(5).row();
        eventDetailsTable.add(detailsTable).colspan(3).pad(10);

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
                    new Team(selectedFootballMatch.home.name, atlas.findRegion(selectedFootballMatch.home.name.split(" ")[0].toLowerCase()), atlas.findRegion(selectedFootballMatch.home.name.split(" ")[0].toLowerCase() + "p")),
                    new Team(selectedFootballMatch.away.name, atlas.findRegion(selectedFootballMatch.away.name.split(" ")[0].toLowerCase()), atlas.findRegion(selectedFootballMatch.away.name.split(" ")[0].toLowerCase() + "p")),
                    Mode.SINGLEPLAYER));
            }
        });
        return playButton;
    }

    private void initializeMap(int zoom) {
        try {
            ZoomXY centerTile = MapRasterTiles.getTileNumber(centerGeolocation.lat, centerGeolocation.lng, zoom);
            mapTiles = MapRasterTiles.getRasterTileZone(centerTile, Constants.NUM_TILES_X, Constants.NUM_TILES_Y);

            beginTile = new ZoomXY(centerTile.zoom, centerTile.x - (Constants.NUM_TILES_X) / 2, centerTile.y - (Constants.NUM_TILES_Y) / 2);

            // Create and populate TiledMap
            if (tiledMap == null) {
                tiledMap = new TiledMap();
            } else {
                disposeLayers();
            }

            MapLayers layers = tiledMap.getLayers();
            TiledMapTileLayer layer = new TiledMapTileLayer(Constants.NUM_TILES_X, Constants.NUM_TILES_Y, MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE);

            int index = 0;
            for (int j = Constants.NUM_TILES_Y - 1; j >= 0; j--) {
                for (int i = 0; i < Constants.NUM_TILES_X; i++) {
                    TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                    cell.setTile(new StaticTiledMapTile(new TextureRegion(mapTiles[index], MapRasterTiles.TILE_SIZE, MapRasterTiles.TILE_SIZE)));
                    layer.setCell(i, j, cell);
                    index++;
                }
            }
            layers.add(layer);

            if (tiledMapRenderer == null) {
                tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
            }

        } catch (IOException e) {
            Gdx.app.log("Map", "Failed to initialize map: " + e.getMessage());
        }
    }

    private void disposeLayers() {
        for (MapLayer layer : tiledMap.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
                for (int x = 0; x < tileLayer.getWidth(); x++) {
                    for (int y = 0; y < tileLayer.getHeight(); y++) {
                        TiledMapTileLayer.Cell cell = tileLayer.getCell(x, y);
                        if (cell != null && cell.getTile() != null) {
                            cell.getTile().getTextureRegion().getTexture().dispose();
                        }
                    }
                }
            }
            tiledMap.getLayers().remove(layer);
        }
    }

    private void clampCameraWithinBounds() {
        float minX = 0, maxX = Constants.MAP_WIDTH;
        float minY = 0, maxY = Constants.MAP_HEIGHT;
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
        if (arenaTexture != null) {
            arenaTexture.dispose();
        }
        if (eventMarkerTexture != null) {
            eventMarkerTexture.dispose();
        }
        stage.dispose();
        skin.dispose();
        MapTileCache.clearCache();
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
        if (zoom < 0.3f) return 10;
        return 9;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        touchPosition.set(x, y, 0);
        camera.unproject(touchPosition);

        // Check for intersection with markers based on active filters
        if (showFootball) {
            for (Match match : footballMatches) {
                if (checkMarkerIntersection(match)) {
                    selectedFootballMatch = match;
                    updateEventDetails(match);
                    return true;
                }
            }
        }

        if (showHandball) {
            for (Match match : handballMatches) {
                if (checkMarkerIntersection(match)) {
                    selectedFootballMatch = match;
                    updateEventDetails(match);
                    return true;
                }
            }
        }

        // Check for user events first
        if (showUserEvents) {
            for (Event event : userEvents) {
                Vector2 markerPosition = MapRasterTiles.getPixelPosition(
                    event.location.lat,
                    event.location.lng,
                    currentZoom,
                    beginTile.x,
                    beginTile.y
                );

                if (touchPosition.x >= markerPosition.x - 24 && touchPosition.x <= markerPosition.x + 24 &&
                    touchPosition.y >= markerPosition.y - 16 && touchPosition.y <= markerPosition.y + 16) {
                    updateUserEventDetails(event);
                    return true;
                }
            }
        }

        selectedFootballMatch = null;
        updateEventDetails(null);
        return false;
    }

    private boolean checkMarkerIntersection(Match match) {
        Vector2 markerPosition = MapRasterTiles.getPixelPosition(
            match.stadium.location.lat,
            match.stadium.location.lng,
            currentZoom,
            beginTile.x,
            beginTile.y
        );

        return touchPosition.x >= markerPosition.x - 24 && touchPosition.x <= markerPosition.x + 24 &&
            touchPosition.y >= markerPosition.y - 16 && touchPosition.y <= markerPosition.y + 16;
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
