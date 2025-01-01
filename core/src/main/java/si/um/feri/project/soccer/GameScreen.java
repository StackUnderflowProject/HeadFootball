package si.um.feri.project.soccer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final float BALL_SPEED = 40f;
    private final SoccerGame game;
    private final AssetManager assetManager;
    private Viewport viewport;
    private Stage stage;
    private Viewport UIviewport;
    private Stage UIstage;
    private Box2DDebugRenderer renderer = new Box2DDebugRenderer();
    private World world;
    private Skin skin;
    private TextureAtlas gameplayAtlas;
    private Array<GameObject> gameObjects;
    private Integer player1;
    private Integer player2;
    private Body goal1;
    private int elapsedTime;
    private Float accumulator;
    private Boolean gameStarted;
    private Ball ball;
    private Goal Goal1;
    private Goal Goal2;
    private Player Player1;
    private Player Player2;
    private boolean kickOff;
    private int kickOffTime = 3;
    private Array<Label> kickOffImage;
    private Label timeLabel;
    private  Label timeValueLabel;
    private Mode mode;
    private Label score1;
    private Label score2;



    public GameScreen(SoccerGame game,Team team1,Team team2,Mode mode) {

        this.game = game;
        this.mode = mode;
        gameStarted = true;
        accumulator = 0f;
        elapsedTime = 300;
        assetManager = game.getAssetManager();
        world = new World(new Vector2(0, -15), true);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        kickOff = true;
        kickOffImage = new Array<>();
        gameObjects = new Array<>();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);
        stage = new Stage(viewport, game.getBatch());
        UIviewport = new StretchViewport(GameConfig.HUD_WIDTH,GameConfig.HUD_HEIGHT);
        UIstage = new Stage(UIviewport,game.getBatch());
        Table rootTable = new Table();
        rootTable.setFillParent(true); // Make the table fill the stage

// Create a label style
        BitmapFont font = new BitmapFont(); // Use default font
        // Create the static "Time" label
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = assetManager.get(AssetDescriptors.TITLE_FONT);
      //  labelStyle.fontColor = Color.BLACK;

        timeLabel = new Label("Time:", labelStyle);
        timeValueLabel = new Label(String.valueOf(elapsedTime), labelStyle);

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


// Create a stack for the images
        Stack stack = new Stack();
        labelStyle.font = assetManager.get(AssetDescriptors.GAMEOVER);
        for(int i = 0;i < kickOffTime;i++){
            Label l = new Label(String.valueOf(i+1),labelStyle);
            l.setVisible(false);
            kickOffImage.add(l);
            stack.add(l);

        }
        kickOffImage.get(2).setVisible(true);

// Add some images to the stack

// Add the images to the stack


// Add the stack to the center of the root table
        rootTable.add(stack).size(50, 50).expand().center().padLeft(30); // Set stack size and center it

// Add the root table to the stage
        UIstage.addActor(rootTable);



        Image background = new Image(gameplayAtlas.findRegion(RegionNames.Textures.FIELD));

        background.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        stage.addActor(background);
        ball = new Ball(gameplayAtlas.findRegion(RegionNames.Textures.BALL1),1.5f,new Vector2(viewport.getWorldWidth()/2,viewport.getWorldHeight()/2),world);
        BodyDef bodyDef = new BodyDef();
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
// Set our body's starting position in the world

        bodyDef.position.set(viewport.getWorldWidth()/2f,viewport.getWorldHeight()/2);
        bodyDef.linearVelocity.y = -10;
        bodyDef.linearVelocity.x = 0f;

// Create our body in the world using our body definition
        //Body body = world.createBody(bodyDef);
        //body.setBullet(true);
// Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(1.5f);
        Sprite sprite = new Sprite(gameplayAtlas.findRegion(RegionNames.Textures.BALL));
        sprite.setPosition(bodyDef.position.x,bodyDef.position.y);
        sprite.setSize(3,3);
        sprite.setOrigin(sprite.getWidth() / 2f,sprite.getHeight()/2f);
// Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.8f;  // No friction
        fixtureDef.restitution = 0.3f; // Make it bounce a little bit
        //body.createFixture(fixtureDef);
        ////body.setUserData("ball");
        //gameObjects.add(new GameObject(sprite,body));*/

        /*Body body2 = physicsBodies.createBody("goal", world, 0.1f, 0.1f);
        body2.setUserData("david");


        body2.setTransform(new Vector2(0f, 10f), 0); // Set the goal's position in the world
           */
        TextureRegion goalTexture =gameplayAtlas.findRegion("goal");
        Goal1 = new Goal(goalTexture,6,10,new Vector2(0,4),world,ID.GOAL1);
        goalTexture.flip(true,false);
     Goal2 = new Goal(goalTexture,6,10,new Vector2(viewport.getWorldWidth() - 6,4),world,ID.GOal2);
     System.out.println("Team1: " + team1.getName() + " Team2: " + team2.getName());
        Player1 = new Player(team1.getPlayer(),5,5,new Vector2(viewport.getWorldWidth()/2 - 15f,6),world,ID.GOal2,Input.Keys.A,Input.Keys.D,Input.Keys.W);
        Player2 = new Player(team2.getPlayer(),5,5,new Vector2(viewport.getWorldWidth()/2 +10f,6),world,ID.GOAL1,Input.Keys.LEFT,Input.Keys.RIGHT,Input.Keys.UP);
       // Gdx.input.setInputProcessor(new InputMultiplexer(Player1.getProcesor(),Player2.getProcesor()));
        /* goal1 = createGoal(world, 0, 4f, 6, 10f, true);
        Sprite goal1Sprite = new Sprite(gameplayAtlas.findRegion("goal"));
        goal1Sprite.setPosition(0,4);
        goal1Sprite.setSize(6, 10); // Set the goal sprite size
        goal1Sprite.setOrigin(goal1Sprite.getWidth() / 2f, goal1Sprite.getHeight() / 2f); // Center the sprite's origin

        Body goal2 = createGoal(world,viewport.getWorldWidth() - 6,4f,6,10f,false);
        gameObjects.add(new GameObject(goal1Sprite,goal1));
        Sprite goal2sprite = new Sprite(gameplayAtlas.findRegion("goal"));
        goal2sprite.setPosition(viewport.getWorldWidth()-6,4);
        goal2sprite.setSize(6, 10); // Set the goal sprite size
        goal2sprite.setOrigin(goal2sprite.getWidth() / 2f, goal2sprite.getHeight() / 2f); // Center the sprite's origin

        goal2sprite.setFlip(true,false);
        gameObjects.add(new GameObject(goal2sprite,goal2));*/
        world.setContactListener(new MyContactListener());
        createBounds(world);
        bodyDef.linearVelocity.x = -20f;
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
    public class MyContactListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
            Fixture fixtureA = contact.getFixtureA();
            Fixture fixtureB = contact.getFixtureB();
            Object userDataA = fixtureA.getBody().getUserData();
            Object userDataB = fixtureB.getBody().getUserData();
            int col = fixtureB.getFilterData().categoryBits | fixtureA.getFilterData().categoryBits;
            switch (col){
                case Bits.GROUND_BIT | Bits.BALL_BIT :
                    //System.out.println("Ground Ball");
                    break;
                case Bits.PLAYER_BIT | Bits.GROUND_BIT:

                    Player player = userDataB instanceof Player ? ((Player) userDataB) : ((Player) userDataA);
                    player.grounded = true;
                    break;
                case Bits.GOALSENSOR_BIT | Bits.BALL_BIT :
                    //System.out.println("Sensor Ball");
                    if (userDataA instanceof Goal) {
                        Goal goalSprite = (Goal) userDataA;
                        Ball ballSprite = (Ball) userDataB;
                        if(goalSprite.id == ID.GOAL1) {
                            Player2.incScore();

                        }
                        else{
                            Player1.incScore();
                        }
                        ballSprite.markReset = true;
                        Player1.markReset = true;
                        Player2.markReset = true;
                        //System.out.println("Collision with Goal: " + (goalSprite.id == ID.GOAL1 ? 1 : 2));
                    } else if (userDataB instanceof Goal){
                        Goal goalSprite = (Goal) userDataB;
                        Ball ballSprite = (Ball) userDataA;
                        ballSprite.markReset = true;
                        Player1.markReset = true;
                        Player2.markReset = true;
                        //System.out.println("Collision with Goal: " + (goalSprite.id == ID.GOAL1 ? 1 : 2));
                    }
                    score1.setText(Player1.getScore());
                    score2.setText(Player2.getScore());
                    kickOff = true;
                    kickOffTime = 3;
                    kickOffImage.get(2).setVisible(true);
                    break;
                default :
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

        private void handleGoal(String id) {
            // Trigger goal handling, such as resetting the ball and updating the score
            System.out.println("Goal Scored! Player: " + id);
            // Reset the ball position or other actions as needed
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
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 0f);

        stage.draw();

        stage.act(delta);


        if(gameStarted){
            // Update the camera and apply to the stage
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
                for (GameObject gameObject : gameObjects) {
                    if ("ball".equals(gameObject.body.getUserData())) {
                        Vector2 impulse = new Vector2(-5f, 0); // Small impulse to the left
                        Vector2 point = gameObject.body.getWorldCenter(); // Apply at the center of the body
                        gameObject.body.applyLinearImpulse(impulse, point, true); // Apply the impulse
                    }
                }
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
                for (GameObject gameObject : gameObjects) {
                    if ("ball".equals(gameObject.body.getUserData())) {
                        Vector2 impulse = new Vector2(5f, 0); // Small impulse to the left
                        Vector2 point = gameObject.body.getWorldCenter(); // Apply at the center of the body
                        gameObject.body.applyLinearImpulse(impulse, point, true); // Apply the impulse
                    }
                }
            }


           for(GameObject gameObject : gameObjects) {
                if ("ball".equals(gameObject.body.getUserData())) {
                    Vector2 bodyPosition = gameObject.body.getPosition();


                    // Update the sprite's position to match the body's position
                    gameObject.sprite.setPosition(bodyPosition.x - gameObject.sprite.getWidth() / 2,
                        bodyPosition.y - gameObject.sprite.getHeight() / 2);
                    gameObject.sprite.setRotation(gameObject.body.getAngle() * MathUtils.radiansToDegrees);
                }
            }
            game.getBatch().begin();
           ball.draw(game.getBatch());
           Goal1.draw(game.getBatch());
            Goal2.draw(game.getBatch());
            Player1.draw(game.getBatch());
            Player2.draw(game.getBatch());
            for(GameObject obj : gameObjects){
                obj.render(game.getBatch());
            }
            game.getBatch().end();

            OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
            camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
            camera.update();

            //renderer.render(world,viewport.getCamera().combined);
        }
        update(delta);
        UIstage.draw();
        UIstage.act();
    }

    public void update(float delta){
        accumulator += delta;

        if (accumulator >= 1) {
            accumulator = 0f; // Reset the accumulator after each second

            if (!kickOff) {
                elapsedTime--;

                // Update the displayed time
                timeValueLabel.setText(String.valueOf(elapsedTime));
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
                    kickOff = false; // End the kickoflif phase
                    //System.out.println(Player1.getScore() + " vs " + Player2.getScore());
                    kickOffImage.get(0).setVisible(false); // Hide the last kickoff image
                }
            }
        }

        if(gameStarted && !kickOff){
            world.step(delta,6,6);

            if(ball.markReset){
                ball.resetBall();
                ball.markReset = false;
            }
            if(Player1.markReset){
                Player1.resetPlayer();
                Player1.markReset = false;
                //System.out.println("Reset");
            }
            if(Player2.markReset){
                Player2.resetPlayer();
                Player2.markReset = false;
            }
            ball.update();
            Player1.handleInput(delta);
            Player1.update(delta);
            if(mode == Mode.LOCALMULTIPLAYER)Player2.handleInput(delta);

            Player2.update(delta);
        }
    }
    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
