package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

abstract public class PowerUp extends Sprite {
    private Animation<TextureRegion> animation;
    private Body body;
    private World world;
    private boolean toDestroy = false;
    private boolean activated = false;

    public PowerUpType getType() {
        return type;
    }

    public void setType(PowerUpType type) {
        this.type = type;
    }

    private PowerUpType type;
    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }



    public PowerUpEffectType getEffectType() {
        return effectType;
    }

    private PowerUpEffectType effectType;

    public boolean isDestroyed() {
        return destroyed;
    }

    abstract void activate();
    abstract void deactivate();

    private boolean destroyed;
    private float elapsedTime = 0;



    public PowerUp(PowerUpType type, TextureAtlas atlas, float width, float height, Vector2 pos, World world,PowerUpEffectType effectType) {
            this.world = world;

            // Initialize animation based on type
            this.effectType = effectType;
            this.type = type;
            Array<TextureAtlas.AtlasRegion> regions;
            switch (type) {
                case BAD:
                    regions = atlas.findRegions(RegionNames.Textures.POWERUPBAD);
                    break;
                case NEUTRAL:
                    regions = atlas.findRegions(RegionNames.Textures.POWERUPNEUTRAL);
                    break;
                case GOOD:
                    regions = atlas.findRegions(RegionNames.Textures.POWERUPGOOD);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown PowerUpType: " + type);
            }

            animation = new Animation<>(0.1f, regions.toArray(TextureRegion.class));


        setSize(width, height);
        setPosition(pos.x , pos.y);
        createBody(world, width, height, pos);
    }

    // Create the head as a circular body
    public void createBody(World world, float width, float height, Vector2 pos) {
        // Create the head (rectangular body with rounded top corners)
        BodyDef headDef = new BodyDef();
        headDef.type = BodyDef.BodyType.KinematicBody;  // Make the head dynamic
        headDef.position.set(pos.x + width / 2, pos.y + height / 2);  // Position head above the player
        body = world.createBody(headDef);
        body.setFixedRotation(true);
        body.setLinearDamping(0.8f);

        CircleShape headShape = new CircleShape();

        headShape.setRadius((width- 1)/ 2);  // Set half-width and height for the rectangle
        FixtureDef headFixture = new FixtureDef();
        headFixture.shape = headShape;
        headFixture.density = 1f;
        headFixture.friction = 3f;
        headFixture.isSensor = true;
        headFixture.filter.categoryBits = Bits.POWERUP_BIT;
        headFixture.filter.maskBits =Bits.BALL_BIT;
        body.createFixture(headFixture);  // Attach fixture to the head body
        body.setUserData(this);

// Clean up the shape
        headShape.dispose();

    }


    public void draw(SpriteBatch batch) {
        if(!destroyed){
            setRegion(animation.getKeyFrame(elapsedTime,true));
            super.draw(batch);
        }
    }

    public void setToDestroy(boolean toDestroy) {
        this.toDestroy = toDestroy;
    }

    // Optionally, you can update the head's position here if needed
    public void update(float delta) {
        elapsedTime += delta;
        if(toDestroy){
            body.setActive(false);
            destroyed = true;
        }
    }
    public void destroy(){
        world.destroyBody(body);
    }
}
