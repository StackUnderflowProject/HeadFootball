package si.um.feri.project.soccer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class Player extends Sprite {
    static public float MAX_SPEED = 20f;
    private TextureRegion texture;

    public int getScore() {
        return score;
    }

    public void incScore() {
        this.score++;
    }

    private int score= 0;

    public Body getHeadBody() {
        return headBody;
    }

    private Body headBody;
    // Circular body for the head
    public boolean grounded = true;
    private Vector2 resetState;
    public Boolean markReset = false;
    public ID id;
    private int leftKey;
    private int rightKey;
    private int jumpKey;



    public Player(TextureRegion region, float width, float height, Vector2 pos, World world, ID id,int leftKey,int rightKey,int jumpKey) {
        setRegion(region);
        this.id = id;
        setSize(width, height);
        setPosition(pos.x, pos.y + height /2);
        createHead(world, width, height, pos);  // Create the head body
        Vector2 position = headBody.getPosition();
        setPosition(position.x - width / 2 , position.y - height / 2);
        this.resetState = pos;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.jumpKey = jumpKey;
    }

    // Create the head as a circular body
    private void createHead(World world, float width, float height, Vector2 pos) {
        // Create the head (rectangular body with rounded top corners)
        BodyDef headDef = new BodyDef();
        headDef.type = BodyDef.BodyType.DynamicBody;  // Make the head dynamic
        headDef.position.set(pos.x + width / 2, pos.y + height / 2);  // Position head above the player
        headBody = world.createBody(headDef);
        headBody.setFixedRotation(true);
        headBody.setLinearDamping(0.8f);
// Create a rectangular shape for the head
        PolygonShape headShape = new PolygonShape();
        headShape.setAsBox((width- 1)/ 2, (height-1) / 2);  // Set half-width and height for the rectangle

        FixtureDef headFixture = new FixtureDef();
        headFixture.shape = headShape;
        headFixture.density = 1f;
        headFixture.friction = 3f;
        headFixture.filter.categoryBits = Bits.PLAYER_BIT;
        headFixture.filter.maskBits = Bits.GROUND_BIT | Bits.BALL_BIT | Bits.PLAYER_BIT;
        headBody.createFixture(headFixture);  // Attach fixture to the head body
        headBody.setUserData(this);

// Clean up the shape
        headShape.dispose();

    }
    public void resetPlayer(){
        headBody.setTransform(resetState.x+getWidth(), resetState.y, 0);

        // Reset velocity and rotation
        headBody.setLinearVelocity(Vector2.Zero);
        headBody.setAngularVelocity(0);

        // Update sprite position
        setPosition(resetState.x - getWidth() / 2, resetState.y - getHeight() / 2);

        // Reset rotation
        setRotation(0);
    }

    public void handleInput(float de) {
        if (Gdx.input.isKeyPressed(leftKey) && headBody.getLinearVelocity().x > -MAX_SPEED) {
            headBody.applyLinearImpulse(new Vector2(-20, 0),headBody.getWorldCenter(), true);  // Move left
        }
        if (Gdx.input.isKeyPressed(rightKey) && headBody.getLinearVelocity().x < MAX_SPEED) {
            headBody.applyLinearImpulse(new Vector2(20, 0),headBody.getWorldCenter(), true);  // Move right
        }
        if (Gdx.input.isKeyJustPressed(jumpKey) && grounded ) {
            headBody.applyLinearImpulse(new Vector2(0, 200), headBody.getWorldCenter(),true);  // Move up
            grounded = false;
        }
    }

    public void draw(SpriteBatch batch) {
        super.draw(batch);  // Draw the player sprite
        // The head is part of the physics world, so no need to manually draw it
    }

    // Optionally, you can update the head's position here if needed
    public void update(float delta) {
        Vector2 position = headBody.getPosition();
        setPosition(position.x - getWidth() / 2 , position.y - getHeight() / 2);
        setRotation((float) Math.toDegrees(headBody.getAngle()));
  }

}
