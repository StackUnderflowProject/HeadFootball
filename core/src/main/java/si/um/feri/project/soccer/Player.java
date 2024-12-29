package si.um.feri.project.soccer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.awt.geom.RectangularShape;

public class Player extends Sprite {
    private float MAX_SPEED = 15f;
    private TextureRegion texture;
    private Body headBody;
    // Circular body for the head
    public boolean grounded = true;
    public ID id;

    public Player(TextureRegion region, float width, float height, Vector2 pos, World world, ID id) {
        setRegion(region);
        this.id = id;
        setSize(width, height);
        setPosition(pos.x, pos.y + height /2);
        createHead(world, width, height, pos);  // Create the head body
    }

    // Create the head as a circular body
    private void createHead(World world, float width, float height, Vector2 pos) {
        // Create the head (rectangular body with rounded top corners)
        BodyDef headDef = new BodyDef();
        headDef.type = BodyDef.BodyType.DynamicBody;  // Make the head dynamic
        headDef.position.set(pos.x + width / 2, pos.y + height / 2);  // Position head above the player
        headBody = world.createBody(headDef);
        headBody.setFixedRotation(true);

// Create a rectangular shape for the head
        PolygonShape headShape = new PolygonShape();
        headShape.setAsBox(width / 2, height / 2);  // Set half-width and height for the rectangle

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
    public void handleInput(float de) {
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && headBody.getLinearVelocity().x > -MAX_SPEED) {
            headBody.applyLinearImpulse(new Vector2(-40, 0),headBody.getWorldCenter(), true);  // Move left
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && headBody.getLinearVelocity().x < MAX_SPEED) {
            headBody.applyLinearImpulse(new Vector2(50, 0),headBody.getWorldCenter(), true);  // Move right
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && grounded ) {
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
