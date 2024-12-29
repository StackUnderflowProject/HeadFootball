package si.um.feri.project.soccer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameScreen extends ScreenAdapter {
    private static final float BALL_SPEED = 40f;
    private final SoccerGame game;
    private final AssetManager assetManager;
    private Viewport viewport;
    private Stage stage;
    private Box2DDebugRenderer renderer = new Box2DDebugRenderer();
    private World world;
    private Skin skin;
    private TextureAtlas gameplayAtlas;
    private Array<GameObject> gameObjects;
    private Integer player1;
    private Integer player2;
    private Body goal1;
    private Integer elapsedTime;
    private Float accumulator;
    private Boolean gameStarted;
    private Ball ball;
    private Goal Goal1;
    private Goal Goal2;
    private Player Player1;
    public GameScreen(SoccerGame game) {

        this.game = game;
        gameStarted = true;
        accumulator = 0f;
        elapsedTime = 0;
        assetManager = game.getAssetManager();
        world = new World(new Vector2(0, -15), true);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);

        gameObjects = new Array<>();
        viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT);

        stage = new Stage(viewport, game.getBatch());
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
        Player1 = new Player(gameplayAtlas.findRegion(RegionNames.Textures.HEAD1),5,5,new Vector2(viewport.getWorldWidth()/2 + 5f,6),world,ID.GOAL1);

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
                    System.out.println("Ground Ball");
                    break;
                case Bits.PLAYER_BIT | Bits.GROUND_BIT:

                    Player player = userDataB instanceof Player ? ((Player) userDataB) : ((Player) userDataA);
                    player.grounded = true;
                    break;
                case Bits.GOALSENSOR_BIT | Bits.BALL_BIT :
                    System.out.println("Sensor Ball");
                    if (userDataA instanceof Goal) {
                        Goal goalSprite = (Goal) userDataA;
                        Ball ballSprite = (Ball) userDataB;
                        ballSprite.markReset = true;
                        System.out.println("Collision with Goal: " + (goalSprite.id == ID.GOAL1 ? 1 : 2));
                    } else if (userDataB instanceof Goal){
                        Goal goalSprite = (Goal) userDataB;
                        Ball ballSprite = (Ball) userDataA;
                        ballSprite.markReset = true;
                        System.out.println("Collision with Goal: " + (goalSprite.id == ID.GOAL1 ? 1 : 2));
                    }

                    break;
                default :
                    System.out.println("Unhandled collision: " + col);

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
            for(GameObject obj : gameObjects){
                obj.render(game.getBatch());
            }
            game.getBatch().end();

            OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();
            camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
            camera.update();

            renderer.render(world,viewport.getCamera().combined);
        }
        update(delta);


    }

    public void update(float delta){
        world.step(delta,6,6);

        if(ball.markReset){
            ball.resetBall();
            ball.markReset = false;
        }
        ball.update();
        Player1.handleInput(delta);
        Player1.update(delta);
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
