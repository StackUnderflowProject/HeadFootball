package si.um.feri.project.soccer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player extends Sprite {
    static public float MAX_SPEED = 20f;

    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }

    private boolean isFrozen = false;
    private TextureRegion ice;



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



    public Player(TextureRegion region,TextureRegion ice, float width, float height, Vector2 pos, World world, ID id,int leftKey,int rightKey,int jumpKey) {
        setRegion(region);
        this.id = id;
        this.ice = ice;
        setSize(width, height);
        setPosition(pos.x, pos.y + height /2);
        createHead(world, width, height, pos);  // Create the head body
        Vector2 position = headBody.getPosition();
        setPosition(position.x - width / 2 , position.y - height / 2 );
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
        /*float radius = Math.min(width, height) / 2f; // Adjust based on the desired size

        float[] vertices = new float[12]; // 6 vertices (x, y) => 12 floats
        for (int i = 0; i < 6; i++) {
            float angle = (float) (Math.PI / 3 * i); // 60 degrees for each vertex
            vertices[i * 2] = radius * (float) Math.cos(angle); // x-coordinate
            vertices[i * 2 + 1] = radius * (float) Math.sin(angle); // y-coordinate
        }*/

// Set the vertices in the shape
        //headShape.set(vertices);
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
        headBody.setTransform(resetState.x + ((id == ID.RIGHT) ? getWidth() : 0), resetState.y, 0);

        // Reset velocity and rotation
        headBody.setLinearVelocity(Vector2.Zero);
        headBody.setAngularVelocity(0);

        // Update sprite position
        setPosition(resetState.x - getWidth() / 2, resetState.y - getHeight() / 2);

        // Reset rotation
        setRotation(0);
        markReset = false;
    }

    public void handleInput(float de) {

        if (Gdx.input.isKeyPressed(leftKey) && headBody.getLinearVelocity().x > -MAX_SPEED && !isFrozen) {
            headBody.applyLinearImpulse(new Vector2(-MAX_SPEED, 0),headBody.getWorldCenter(), true);  // Move left
        }
        if (Gdx.input.isKeyPressed(rightKey) && headBody.getLinearVelocity().x < MAX_SPEED && !isFrozen) {
            headBody.applyLinearImpulse(new Vector2(MAX_SPEED, 0),headBody.getWorldCenter(), true);  // Move right
        }
        if (Gdx.input.isKeyJustPressed(jumpKey) && grounded ) {
            headBody.applyLinearImpulse(new Vector2(0, 200), headBody.getWorldCenter(),true);  // Move up
            grounded = false;
        }
    }
    public void handleAiMovement(float deltaTime, Vector2 targetPosition) {
        // Define maximum speed and impulse values for AI
        final float MAX_SPEED = 5f;
        final float MOVE_IMPULSE = 20f;
        final float JUMP_IMPULSE = 200f;
        final float desiredDistance = 10f;
        // Get the AI's position
        Vector2 aiPosition = headBody.getPosition();

        // Calculate the distance between AI and the target
        float distanceToTarget = aiPosition.dst(targetPosition);

        // Check if the AI needs to move closer or farther
        if (distanceToTarget > desiredDistance) {
            // Move closer to the target
            if (targetPosition.x < aiPosition.x && headBody.getLinearVelocity().x > -MAX_SPEED && !isFrozen) {
                // Move left
                headBody.applyLinearImpulse(new Vector2(-MOVE_IMPULSE, 0), headBody.getWorldCenter(), true);
            } else if (targetPosition.x > aiPosition.x && headBody.getLinearVelocity().x < MAX_SPEED && !isFrozen) {
                // Move right
                headBody.applyLinearImpulse(new Vector2(MOVE_IMPULSE, 0), headBody.getWorldCenter(), true);
            }
        } else if (distanceToTarget < desiredDistance * 0.8f) {
            // Move farther away from the target (adds a buffer zone to prevent oscillation)
            if (targetPosition.x < aiPosition.x && headBody.getLinearVelocity().x < MAX_SPEED && !isFrozen) {
                // Move right (away from target)
                headBody.applyLinearImpulse(new Vector2(MOVE_IMPULSE, 0), headBody.getWorldCenter(), true);
            } else if (targetPosition.x > aiPosition.x && headBody.getLinearVelocity().x > -MAX_SPEED && !isFrozen) {
                // Move left (away from target)
                headBody.applyLinearImpulse(new Vector2(-MOVE_IMPULSE, 0), headBody.getWorldCenter(), true);
            }
        }

        // Optional: Add jumping logic for obstacles or variety
        if (shouldJump() && grounded) {
            headBody.applyLinearImpulse(new Vector2(0, JUMP_IMPULSE), headBody.getWorldCenter(), true);
            grounded = false;
        }
    }

    // Simple condition to decide if the AI should jump
    private boolean shouldJump() {
        // Example: Jump with a 10% chance per frame, or add your custom condition
        return Math.random() < 0.1;
    }






    public void draw(SpriteBatch batch) {
        float width = super.getWidth() * 0.8f;
        float heght = super.getWidth() *0.8f;

        float iconX = getX()+width/4;
        float iconY = getY();
        super.draw(batch);  // Draw the player sprite
        batch.setColor(1f, 1f, 1f, 0.8f); // Set the batch color with alpha

        if(isFrozen)batch.draw(ice, iconX, iconY, width, heght);
        batch.setColor(1f, 1f, 1f, 1); // Set the batch color with alpha


        // The head is part of the physics world, so no need to manually draw it
    }

    // Optionally, you can update the head's position here if needed
    public void update(float delta) {
        Vector2 position = headBody.getPosition();
        setPosition(position.x - getWidth() / 2 , position.y - getHeight() / 2);
        setRotation((float) Math.toDegrees(headBody.getAngle()));
  }

}
