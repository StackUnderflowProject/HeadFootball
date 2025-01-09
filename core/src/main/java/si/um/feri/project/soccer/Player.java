package si.um.feri.project.soccer;

import static com.badlogic.gdx.math.MathUtils.random;
import static si.um.feri.project.soccer.GameConfig.JUMP_IMPULSE;
import static si.um.feri.project.soccer.GameConfig.MAX_SPEED;
import static si.um.feri.project.soccer.GameConfig.MAX_SPEED_IN_AIR;
import static si.um.feri.project.soccer.GameConfig.MOVE_IMPULSE;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

public class Player extends Sprite {

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
    private boolean isFacingLeft = false;
    private boolean lastFacingLeft = false; // Initially facing right

    private int leftKey;
    private int rightKey;
    private int jumpKey;
     private Random random = new Random();
     private ParticleEffect particleEffect = new ParticleEffect();
    private float speedMultiplier = 1;
    private TextureAtlas atlas= SoccerGame.assetManager.get(AssetDescriptors.GAMEPLAY) ;



    public Player(TextureRegion region,TextureRegion ice, float width, float height, Vector2 pos, World world, ID id,int leftKey,int rightKey,int jumpKey) {
        setRegion(region);
        this.id = id;
        this.ice = ice;
        this.particleEffect.load(Gdx.files.internal("jump.p"),Gdx.files.internal("") // Directory containing particle textures
        );
        particleEffect.scaleEffect(0.2f);
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
        headFixture.friction = 2f;
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
        float MAX_SPEED = (grounded) ? GameConfig.MAX_SPEED : MAX_SPEED_IN_AIR;
        MAX_SPEED *= speedMultiplier;
        if (Gdx.input.isKeyPressed(leftKey) && headBody.getLinearVelocity().x > -MAX_SPEED && !isFrozen) {
            headBody.applyLinearImpulse(new Vector2(-MOVE_IMPULSE * speedMultiplier, 0),headBody.getWorldCenter(), true);  // Move left
        }
        if (Gdx.input.isKeyPressed(rightKey) && headBody.getLinearVelocity().x < MAX_SPEED && !isFrozen) {
            headBody.applyLinearImpulse(new Vector2(MOVE_IMPULSE * speedMultiplier, 0),headBody.getWorldCenter(), true);  // Move right
        }
        if (Gdx.input.isKeyJustPressed(jumpKey) && grounded ) {
            headBody.applyLinearImpulse(new Vector2(0, JUMP_IMPULSE), headBody.getWorldCenter(),true);  // Move up
            float randomPitch = 0.8f + (float) Math.random() * 0.4f;
            SoundManager.jump.play(1.0f, randomPitch, 0.0f);
            grounded = false;
            particleEffect.setPosition(getX() + getWidth() / 2, getY()); // Set position to match player's current position
            particleEffect.start();
        }
    }
    public void handleAiMovement(float deltaTime, Vector2 targetPosition) {
        // Define maximum speed and impulse values for AI

        // Get the AI's position
        Vector2 aiPosition = headBody.getPosition();

        // Calculate the distance between AI and the target
        float distanceToTarget = aiPosition.dst(targetPosition);
        // Check if the AI needs to move closer or farther
        if (distanceToTarget > GameConfig.AI_DESIRED_DISTANCE  || (aiPosition.x > GameConfig.WORLD_WIDTH - GameConfig.GOALWIDTH *2)) {
            // Move closer to the target
            if (targetPosition.x < aiPosition.x && headBody.getLinearVelocity().x > -MAX_SPEED && !isFrozen) {
                // Move left
                headBody.applyLinearImpulse(new Vector2(-MOVE_IMPULSE, 0), headBody.getWorldCenter(), true);
            } else if (targetPosition.x > aiPosition.x && headBody.getLinearVelocity().x < MAX_SPEED && !isFrozen) {
                // Move right
                headBody.applyLinearImpulse(new Vector2(MOVE_IMPULSE, 0), headBody.getWorldCenter(), true);
            }
        } else if (distanceToTarget < GameConfig.AI_DESIRED_DISTANCE * 0.8f && (aiPosition.x < GameConfig.WORLD_WIDTH - GameConfig.GOALWIDTH *2 && aiPosition.x >  GameConfig.GOALWIDTH *2)) {
            // Move farther away from the target (adds a buffer zone to prevent oscillation)
            if (targetPosition.x < aiPosition.x && headBody.getLinearVelocity().x < MAX_SPEED && !isFrozen) {
                // Move right (away from target)
                headBody.applyLinearImpulse(new Vector2(MOVE_IMPULSE, 0), headBody.getWorldCenter(), true);
            } else if (targetPosition.x > aiPosition.x && headBody.getLinearVelocity().x > -MAX_SPEED && !isFrozen) {
                // Move left (away from target)
                headBody.applyLinearImpulse(new Vector2(-MOVE_IMPULSE, 0), headBody.getWorldCenter(), true);
            }
        }
        System.out.println("Jump count: " + shouldJump()); // Should be ~10% of 1000

        if (shouldJump() && grounded) {
            headBody.applyLinearImpulse(new Vector2(0, JUMP_IMPULSE), headBody.getWorldCenter(), true);
            float randomPitch = 0.8f + (float) Math.random() * 0.4f;

            SoundManager.jump.play(1.0f, randomPitch, 0.0f);
            grounded = false;
        }
    }

    // Simple condition to decide if the AI should jump
    private boolean shouldJump() {
        // Example: Jump with a 10% chance per frame, or add your custom condition
        double randomDouble = random.nextDouble(); // 0.0 to 1.0

        return randomDouble > 0.999d;
    }






    public void draw(SpriteBatch batch) {
        float width = super.getWidth() * 0.8f;
        float heght = super.getWidth() *0.8f;

        float iconX = getX()+width/4;
        float iconY = getY();
        /*if (isFacingLeft && !isFlipX()) {
            flip(true, false); // Flip horizontally
        } else if (!isFacingLeft && isFlipX()) {
            flip(true, false); // Reset to not flipped
        }     */   super.draw(batch);  // Draw the player sprite
        batch.setColor(1f, 1f, 1f, 0.8f); // Set the batch color with alpha
        // Update and draw the particle effect
        if (!particleEffect.isComplete()) { // Check if the effect is still playing
            particleEffect.setPosition(getX() + getWidth() / 2, getY());
            particleEffect.update(Gdx.graphics.getDeltaTime());
            particleEffect.draw(batch);
        }

        if(isFrozen)batch.draw(ice, iconX, iconY, width, heght);
        batch.setColor(1f, 1f, 1f, 1); // Set the batch color with alpha
        batch.draw(atlas.findRegion(
            (speedMultiplier == 1) ? RegionNames.Textures.SPEEDNEUTRAL :
                (speedMultiplier > 1) ? RegionNames.Textures.SPEEDGOOD :
                    RegionNames.Textures.SPEEDBAD
        ),getX() + getWidth() /2 ,getY() + getHeight() / 2,getWidth() * 0.2f,getHeight()*0.2f);
        // The head is part of the physics world, so no need to manually draw it
    }

    // Optionally, you can update the head's position here if needed
    public void update(float delta) {
        Vector2 position = headBody.getPosition();
        setPosition(position.x - getWidth() / 2 , position.y - getHeight() / 2);
        setRotation((float) Math.toDegrees(headBody.getAngle()));
        if (headBody.getLinearVelocity().x < 0) {
            isFacingLeft = true;
            lastFacingLeft = true;
        } else if (headBody.getLinearVelocity().x > 0) {
            isFacingLeft = false;
            lastFacingLeft = false;
        } else {
            // No horizontal movement; retain the last direction
            isFacingLeft = lastFacingLeft;
        }  }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }
}
