package si.um.feri.project.soccer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private final SoccerGame game;
    private final AssetManager assetManager;
    //private final Array<PowerUp> powerUp;

    private Viewport viewport;
    private Stage stage;
    private Viewport UIviewport;
    private Stage UIstage;
    private Box2DDebugRenderer renderer = new Box2DDebugRenderer();
    private World world;
    private Skin skin;
    private TextureAtlas gameplayAtlas;
    private Array<GameObject> gameObjects;
    private int elapsedTime;
    private Float accumulator;
    private Stack stack;

    private Goal Goal1;
    private Goal Goal2;
    private Player Player1;
    private Player Player2;
    private GameState state;
    private int kickOffTime = 3;
    private Array<Label> kickOffImage;
    private Label timeLabel;
    private  Label timeValueLabel;
    private Mode mode;
    private Label score1;
    private Label score2;
    private Team team1;
    private  Team team2;
    private SpriteBatch batch;
    private FPSLogger fpsLogger;


    public GameScreen(SoccerGame game,Team team1,Team team2,Mode mode) {

        this.game = game;
        this.mode = mode;
        this.batch = game.getBatch();
        accumulator = 0f;
        elapsedTime = 120;
        assetManager = game.getAssetManager();
        world = new World(new Vector2(0, -15), true);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        fpsLogger = new FPSLogger();
        kickOffImage = new Array<>();
        state = GameState.BEGIN;
        gameObjects = new Array<>();
        this.team2 = team2;
        this.team1 = team1;
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        stage = new Stage(viewport, game.getBatch());
        UIviewport = new StretchViewport(GameConfig.HUD_WIDTH,GameConfig.HUD_HEIGHT);
        UIstage = new Stage(UIviewport,game.getBatch());
        InputMultiplexer ml = new InputMultiplexer(UIstage,stage);
        Gdx.input.setInputProcessor(ml);

        Table rootTable = new Table();
        rootTable.setFillParent(true); // Make the table fill the stage

        BallsManager.initialize(gameplayAtlas,new Vector2(viewport.getWorldWidth() / 2f,viewport.getWorldHeight() / 2f),world);
        GoalManager.initialize(gameplayAtlas,world,viewport.getWorldWidth());
// Create a label style
        BitmapFont font = new BitmapFont(); // Use default font
        // Create the static "Time" label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = assetManager.get(AssetDescriptors.TITLE_FONT);
      //  labelStyle.fontColor = Color.BLACK;

        timeLabel = new Label("Time:", labelStyle);
        int minutes = elapsedTime / 60;
        int seconds = elapsedTime % 60;

// Format the time as mm:ss
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timeValueLabel = new Label(timeFormatted, labelStyle);

// Create a container for the labels
        Table labelTable = new Table().debugTable(); // Debug for layout visualization
        labelTable.pad(0); // Remove extra padding


// Load photos (textures)
        TextureRegionDrawable photo1 = new TextureRegionDrawable(team1.getTextureRegion()); // Team 1 photo
        TextureRegionDrawable photo2 = new TextureRegionDrawable(team2.getTextureRegion()); // Team 2 photo

// Create Image objects for the photos
        Pixmap pixmap = extractPixmapFromTextureRegion(team1.getTextureRegion());


        pixmap = applyMask(pixmap);

        /* Load the pixel information of the Pixmap into a Texture for drawing. */
        Texture masked = new Texture(pixmap);
        Image photo1Image = new Image(masked);
        pixmap = extractPixmapFromTextureRegion(team2.getTextureRegion());


        pixmap = applyMask(pixmap);

        /* Load the pixel information of the Pixmap into a Texture for drawing. */
        masked = new Texture(pixmap);
        Image photo2Image = new Image(masked);

// Create a table for Team 1
        Table teamTable = new Table();
        labelStyle.font.getData().setScale(0.3f); // Reduce the font size by adjusting the scale

        teamTable.add(photo1Image).pad(5).size(50, 50).row(); // Photo in the first column
        //teamTable.add(new Label(team1.getName(), labelStyle)).padTop(5); // Team name in the second column

// Create a table for the time
        Pixmap pixmap1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap1.setColor(Color.valueOf("210555" )); // Set color #3A3960
        pixmap1.fill();

// Create a Texture from the Pixmap
        Texture texture = new Texture(pixmap1);

// Create a Drawable from the Texture
        Drawable colorBackground = new TextureRegionDrawable(texture);

        Table timeTable = new Table();
        timeTable.add(timeValueLabel).padLeft(30).padRight(30); // Dynamic time value label
        timeTable.setColor(Color.BLACK);
        Table teamTable1 = new Table();
        teamTable1.add(photo2Image).pad(5).size(50, 50).row(); // Photo in the first column
        //teamTable1.add(new Label(team2.getName(), labelStyle)).padTop(5);
// Add the teamTable and timeTable to the labelTable
        // Team 2 photo in the third column
        Table scoreTable = new Table();
        labelTable.setBackground(colorBackground);
        pixmap1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap1.setColor(Color.valueOf("3E5879")); // Set color #3A3960
        pixmap1.fill();

// Create a Texture from the Pixmap
        texture = new Texture(pixmap1);

// Create a Drawable from the Texture
        colorBackground = new TextureRegionDrawable(texture);
        timeTable.setBackground(colorBackground);
        score1 = new Label("0",labelStyle);
        score2 = new Label("0",labelStyle);;
        scoreTable.add(score1).colspan(1).pad(5); // Takes 1 column
        scoreTable.add(timeTable).colspan(4); // Takes 8 columns, double the space of score1 and score2 combined
        scoreTable.add(score2).colspan(1).pad(5).row(); // Takes 1 column
        scoreTable.add(new Label(team1.getName(), labelStyle)).colspan(2);
        scoreTable.add(new Actor()).colspan(2);
        scoreTable.add(new Label(team2.getName(), labelStyle)).colspan(2);

        labelTable.add(teamTable).pad(5); // Team info in the first column
        labelTable.add(scoreTable).pad(5).padLeft(10).padRight(10); // Time info in the second column
        labelTable.add(teamTable1).pad(5);

// Optional: Center the labelTable
        labelTable.center();

// Add the label table to the root table at the top with minimal padding
        rootTable.add(labelTable).top().padTop(10).padLeft(1).padRight(1);

// Move to the next row for additional UI elements
        rootTable.row();

        Image im = new Image(gameplayAtlas.findRegion((this.mode == Mode.SINGLEPLAYER) ? RegionNames.Textures.SINGLEINTRO : RegionNames.Textures.MULTIINTRO));
        im.setName("stack");
// Create a stack for the images

// Add some images to the stack

// Add the images to the stack

// Add the stack to the center of the root table


        rootTable.add(im).size(UIviewport.getWorldWidth()/2,UIviewport.getWorldHeight()/3).expand().center(); // Set stack size and center it
        rootTable.row();

// Add the root table to the stage
        ImageButton.ImageButtonStyle musicButtonStyle = new ImageButton.ImageButtonStyle();
        musicButtonStyle.up = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.ON)); // Music on texture
        musicButtonStyle.checked = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.OFF)); // Music off texture
        ImageButton musicButton = new ImageButton(musicButtonStyle);
        musicButton.setChecked(GamePreferences.loadMusicVolume() == 0);
        musicButton.setTransform(true);
        musicButton.setTouchable(Touchable.enabled);
        musicButton.setOrigin(Align.center);
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("PAUSE");
                GamePreferences.toggleMusic(); // Assuming you have a method in SoccerGame for toggling music
                SoccerGame.loadMusic();
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                musicButton.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f)); // Scale up to 120% over 0.1 seconds
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                musicButton.addAction(Actions.scaleTo(1f, 1f, 0.1f)); // Scale back to normal
            }
        });

        ;ImageButton.ImageButtonStyle pauseButtonStyle = new ImageButton.ImageButtonStyle();
        rootTable.row();
        pauseButtonStyle.up = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.PAUSE)); // Music on texture
        pauseButtonStyle.checked = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.PAUSE)); // Music off texture
        ImageButton pauseButton = new ImageButton(pauseButtonStyle);
       // pauseButton.setChecked(GamePreferences.loadMusicVolume() == 0);
        pauseButton.setTransform(true);
        pauseButton.setTouchable(Touchable.enabled);
        pauseButton.setOrigin(Align.center);
        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("PAUSE");
                if(state != GameState.BEGIN & state != GameState.KICKOFF){
                    state = (state == GameState.PAUSED) ? GameState.STARTED : GameState.PAUSED;
                };
            }
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                pauseButton.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f)); // Scale up to 120% over 0.1 seconds
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                pauseButton.addAction(Actions.scaleTo(1f, 1f, 0.1f)); // Scale back to normal
            }
        });


        // Set the input processor
        Image background = new Image(gameplayAtlas.findRegion(RegionNames.Textures.FIELD));

        background.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        stage.addActor(background);



        // stage.addActor(layoutTable);

        BodyDef bodyDef = new BodyDef();
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
// Set our body's starting position in the world

        bodyDef.position.set(viewport.getWorldWidth()/2f,viewport.getWorldHeight()/2);
        bodyDef.linearVelocity.y = 0;
        bodyDef.linearVelocity.x = 0f;

        Player1 = new Player(team1.getPlayer(),gameplayAtlas.findRegion(RegionNames.Textures.ICE),5,5,new Vector2(viewport.getWorldWidth()/2 - 15f,6),world,ID.LEFT,Input.Keys.A,Input.Keys.D,Input.Keys.W);
        Player2 = new Player(team2.getPlayer(),gameplayAtlas.findRegion(RegionNames.Textures.ICE),5,5,new Vector2(viewport.getWorldWidth()/2 +10f,6),world,ID.RIGHT,Input.Keys.LEFT,Input.Keys.RIGHT,Input.Keys.UP);
        PlayerManager.initialize(Player1,Player2);
        PowerUpManager.initialize(gameplayAtlas,viewport,world);

        world.setContactListener(new MyContactListener());
        createBounds(world);
        Table btn = new Table();
        btn.add(musicButton).padRight(5);
        btn.add(pauseButton).padRight(5);
        rootTable.add(btn).right().pad(10,0,0,10);
        UIstage.addActor(rootTable);

    }
    public Pixmap extractPixmapFromTextureRegion(TextureRegion textureRegion) {
        TextureData textureData = textureRegion.getTexture().getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        Pixmap pixmap = new Pixmap(
            textureRegion.getRegionWidth(),
            textureRegion.getRegionHeight(),
            textureData.getFormat()
        );
        pixmap.drawPixmap(
            textureData.consumePixmap(), // The other Pixmap
            0, // The target x-coordinate (top left corner)
            0, // The target y-coordinate (top left corner)
            textureRegion.getRegionX(), // The source x-coordinate (top left corner)
            textureRegion.getRegionY(), // The source y-coordinate (top left corner)
            textureRegion.getRegionWidth(), // The width of the area from the other Pixmap in pixels
            textureRegion.getRegionHeight() // The height of the area from the other Pixmap in pixels
        );
        return pixmap;
    }

    private Pixmap applyMask(Pixmap source) {
        /* Create a Pixmap to store the mask information, at the end it will
         * contain the result. */
        Pixmap result = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);

        /* This setting lets us overwrite the pixels' transparency. */
        result.setBlending(Pixmap.Blending.None);

        /* Ignore RGB values unless you want funky results, alpha is for the mask. */
        result.setColor(new Color(1f, 1f, 1f, 1f));

        /* Draw a circle to our mask, any shape is possible since
         * you can draw individual pixels to the Pixmap. */
        result.fillCircle(source.getWidth() / 2, source.getHeight() / 2, source.getHeight() / 2);



        /* We can also define the mask by loading an image:
         * result = new Pixmap(new FileHandle("image.png")); */

        /* Decide the color of each pixel using the AND bitwise operator. */
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                result.drawPixel(x, y, source.getPixel(x, y) & result.getPixel(x, y));
            }
        }

        return result;
    }

    private Pixmap createBlackPixmap() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1); // Black color with full opacity.
        pixmap.fill();
        return pixmap;
    }

    public Stage getStage() {
        return stage;
    }

    public class MyContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            Object userDataA = fixtureA.getBody().getUserData();
            Object userDataB = fixtureB.getBody().getUserData();
            int col = fixtureB.getFilterData().categoryBits | fixtureA.getFilterData().categoryBits;
            switch (col){
                case Bits.BALL_BIT | Bits.POWERUP_BIT:
                {
                    PowerUp pu = userDataB instanceof PowerUp ? ((PowerUp) userDataB) : ((PowerUp) userDataA);
                    pu.activate();
                    //PowerUpManager.move(pu);
                    pu.setToDestroy(true);
                    break;
                }
                case Bits.GROUND_BIT | Bits.BALL_BIT :
                {
                    break;
                }
                case Bits.PLAYER_BIT | Bits.GROUND_BIT:
                {
                    Player player = userDataB instanceof Player ? ((Player) userDataB) : ((Player) userDataA);
                    player.grounded = true;
                    break;
                }
                case Bits.GOALSENSOR_BIT | Bits.BALL_BIT :{
                    Goal goalSprite = userDataB instanceof Goal ? ((Goal) userDataB) : ((Goal) userDataA);
                    Ball ballSprite = userDataB instanceof Ball ? ((Ball) userDataB) : ((Ball) userDataA);


                    if(goalSprite.id == ID.LEFT) {
                        Player2.incScore();

                    }
                    else{
                        Player1.incScore();
                    }
                    ballSprite.markReset = true;
                    Player1.markReset = true;
                    Player2.markReset = true;
                    score1.setText(Player1.getScore());
                    score2.setText(Player2.getScore());
                    state = GameState.KICKOFF;
                    if(elapsedTime == 0) {state = GameState.OVERTIME;timeValueLabel.setText("OVER TIME");};
                    if(state == GameState.OVERTIME && Player1.getScore() != Player2.getScore())game.setScreen(new GameOverScreen(game,team1,team2,Player1,Player2));                    kickOffTime = 3;
                    kickOffImage.get(2).setVisible(true);
                    BallsManager.toggleBall(BallType.NORMAL);
                    GoalManager.toggleGoal(GoalType.NORMAL,ID.LEFT);
                    GoalManager.toggleGoal(GoalType.NORMAL,ID.RIGHT);
                    PlayerManager.unfreezePlayer(ID.LEFT);
                    PlayerManager.unfreezePlayer(ID.RIGHT);

                    PowerUpManager.setToClear(true);

                    BallsManager.reset = true;
                    break;
                }

                case Bits.PLAYER_BIT | Bits.BALL_BIT:
                {
                    Player player = userDataB instanceof Player ? ((Player) userDataB) : ((Player) userDataA);
                    BallsManager.lastTouched = player.id;
                    break;
                }
                default :
                    //BallsManager.toggleBall(BallType.DULL);

                    //System.out.println("Unhandled collision: " + col);

            }
            // Debug print to check what's happening

        }

        @Override
        public void endContact(Contact contact) {



            // Debug prin

        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            // You can handle collision details before the physics solver (optional)
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
            // Can be used for post-solve logic (optional)
        }

    }



    public void createBounds(World world){
        // Ground (bottom)
        BodyDef groundBodyDef = new BodyDef();
        groundBodyDef.type = BodyDef.BodyType.StaticBody;
        groundBodyDef.position.set(0, 4);
        Body groundBody = world.createBody(groundBodyDef);
        EdgeShape groundShape = new EdgeShape();
        groundShape.set(0f, 0f, viewport.getWorldWidth(), 0f); // Length of the ground
        FixtureDef groundFixtureDef = new FixtureDef();
        groundFixtureDef.shape = groundShape;
        groundFixtureDef.friction = 1f;
        groundFixtureDef.filter.categoryBits = Bits.GROUND_BIT;

        groundBody.createFixture(groundFixtureDef);
        ;

        // Left Wall
        BodyDef wallLeftBodyDef = new BodyDef();
        wallLeftBodyDef.type = BodyDef.BodyType.StaticBody;
        wallLeftBodyDef.position.set(0.1f, 0.1f);
        Body wallLeftBody = world.createBody(wallLeftBodyDef);
        EdgeShape leftWallShape = new EdgeShape();
        leftWallShape.set(0f, 0f, 0f, viewport.getWorldHeight()); // Left wall
        FixtureDef wallLeftFixtureDef = new FixtureDef();
        wallLeftFixtureDef.shape = leftWallShape;
        wallLeftFixtureDef.friction = 1f;
        wallLeftFixtureDef.filter.categoryBits = Bits.GROUND_BIT;
        wallLeftFixtureDef.filter.maskBits = Bits.BALL_BIT | Bits.PLAYER_BIT;
        wallLeftBody.createFixture(wallLeftFixtureDef);

        // Right Wall
        BodyDef wallRightBodyDef = new BodyDef();
        wallRightBodyDef.type = BodyDef.BodyType.StaticBody;
        wallRightBodyDef.position.set(viewport.getWorldWidth()-0.1f, 0);
        Body wallRightBody = world.createBody(wallRightBodyDef);
        groundShape.set(0f, 0f,0f, viewport.getWorldHeight()); // Right wall
        FixtureDef wallRightFixtureDef = new FixtureDef();
        wallRightFixtureDef.shape = groundShape;
        wallRightFixtureDef.friction = 1f;
        wallRightFixtureDef.filter.categoryBits = Bits.GROUND_BIT;
        wallRightFixtureDef.filter.maskBits = Bits.BALL_BIT | Bits.PLAYER_BIT;

        wallRightBody.createFixture(wallRightFixtureDef);

        // Ceiling
        BodyDef ceilBodyDef = new BodyDef();
        ceilBodyDef.type = BodyDef.BodyType.StaticBody;
        ceilBodyDef.position.set(0, viewport.getWorldHeight()-0.1f); // Adjusted
        Body ceilBody = world.createBody(ceilBodyDef);
        groundShape.set(0f, 0, viewport.getWorldWidth(), 0); // Ceiling shape
        FixtureDef ceilFixtureDef = new FixtureDef();
        ceilFixtureDef.shape = groundShape;
        ceilFixtureDef.filter.maskBits = Bits.BALL_BIT | Bits.PLAYER_BIT;
        ceilFixtureDef.filter.categoryBits = Bits.GROUND_BIT;
        ceilFixtureDef.friction = 0.0f;
        ceilBody.createFixture(ceilFixtureDef);

        // Dispose of shape after all fixtures are created
    }



    public static Texture toTexture(TextureRegion region) {
        Pixmap pixmap = new Pixmap(region.getRegionWidth(), region.getRegionHeight(), Pixmap.Format.RGBA8888);
        if (!region.getTexture().getTextureData().isPrepared()) {
            region.getTexture().getTextureData().prepare();
        }
        Pixmap texturePixmap = region.getTexture().getTextureData().consumePixmap();
        pixmap.drawPixmap(texturePixmap, 0, 0, region.getRegionX(), region.getRegionY(), region.getRegionWidth(), region.getRegionHeight());
        Texture newTexture = new Texture(pixmap);

        texturePixmap.dispose();
        pixmap.dispose();
        return newTexture;
    }

    @Override
    public void show() {
        skin = assetManager.get(AssetDescriptors.UI_SKIN);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

    }

    @Override
    public void resize(int width, int height) {
        // Update the viewport whenever the window is resized

        viewport.update(width, height, true);
        UIviewport.update(width, height,false);


    }

    @Override
    public void render(float delta) {
        //System.out.println(Gdx.graphics.getFramesPerSecond());
        ScreenUtils.clear(0f, 0f, 0f, 0f);

        stage.act(delta);
        stage.draw();

        batch.begin();
        Player1.draw(batch);
        Player2.draw(batch);
        PowerUpManager.draw(batch);
        //Goal1.draw(batch);
        //Goal2.draw(batch);
        GoalManager.draw(batch);

        BallsManager.draw(batch);
        batch.end();
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && state == GameState.BEGIN){
            state = GameState.KICKOFF;
            stack = new Stack();
            Label.LabelStyle labelStyle = new Label.LabelStyle();
            labelStyle.font = assetManager.get(AssetDescriptors.TITLE_FONT);
            labelStyle.font = assetManager.get(AssetDescriptors.GAMEOVER);
            for(int i = 0;i < kickOffTime;i++){
                Label l = new Label(String.valueOf(i+1),labelStyle);
                l.setVisible(false);
                kickOffImage.add(l);
                stack.add(l);

            }
            kickOffImage.get(2).setVisible(true);
            Actor foundActor = UIstage.getRoot().findActor("stack");
                Table parentTable = (Table) foundActor.getParent();
                parentTable.debugAll();
            Cell cell = parentTable.getCell(foundActor);

            cell.setActor(stack).padLeft(20).center();

            cell.size(20, 20);
            stack.setVisible(true);

            cell.align(Align.center);

        }
        if(state != GameState.BEGIN  & state != GameState.PAUSED){
            update(delta);
            renderer.render(world,viewport.getCamera().combined);
        }


        UIstage.act(delta);
        UIstage.draw();
    }

    public void update(float delta){
        if(elapsedTime == 0) {state = GameState.OVERTIME;timeValueLabel.setText("OVER TIME");};
        if(state == GameState.OVERTIME && Player1.getScore() != Player2.getScore())game.setScreen(new GameOverScreen(game,team1,team2,Player1,Player2));
        accumulator += delta;

        if (accumulator >= 1) {
            accumulator = 0f; // Reset the accumulator after each second

            if (state != GameState.KICKOFF && !(elapsedTime == 0)) {
                elapsedTime--;
                elapsedTime = Math.max(0,elapsedTime);
                // Update the displayed time
                int minutes = elapsedTime / 60;
                int seconds = elapsedTime % 60;

// Format the time as mm:ss
                String timeFormatted = String.format("%02d:%02d", minutes, seconds);
                timeValueLabel.setText(String.valueOf(timeFormatted));
            } else {
                kickOffTime--;

                // Ensure `kickOffTime` logic and visibility handling
                if (kickOffTime >= 0) {
                    if (kickOffTime + 1 < kickOffImage.size) {
                        kickOffImage.get(kickOffTime + 1).setVisible(false); // Hide the previous image
                    }
                    kickOffImage.get(kickOffTime).setVisible(true); // Show the current image
                }

                if (kickOffTime == -1) {
                    state = GameState.STARTED; // End the kickoflif phase
                    //System.out.println(Player1.getScore() + " vs " + Player2.getScore());
                    kickOffImage.get(0).setVisible(false); // Hide the last kickoff image
                }
            }
        }

        if(state != GameState.KICKOFF){
            world.step(delta,6,6);
            if(PowerUpManager.isToClear()) PowerUpManager.clear();
            BallsManager.update();
            GoalManager.update();
            PowerUpManager.update(delta);
            if(Player1.markReset){
                Player1.resetPlayer();
            }
            if(Player2.markReset){
                Player2.resetPlayer();
            }
            Player1.handleInput(delta);
            Player1.update(delta);
            if(mode == Mode.LOCALMULTIPLAYER)Player2.handleInput(delta);
            else {
                Player2.handleAiMovement(delta,BallsManager.getCurrentBallPosition());
            }
            Player2.update(delta);
        }
    }
    @Override
    public void hide() {
        dispose();
    }
    private void disposeWorld(World world) {
        if (world != null) {
            Array<Body> bodies= new Array<>();
            world.getBodies(bodies);
            for (Body body :  bodies){
                world.destroyBody(body);
            }
            world.setContactListener(null);
            world.dispose();
            world = null;
        }
    }


    @Override
    public void dispose() {
        stage.dispose();
        UIstage.dispose();

    }
}
