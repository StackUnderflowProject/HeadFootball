package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;

public class Ball extends Sprite {

    private TextureRegion texture;
    private Body body;
    private Vector2 resetState;
    public boolean active = false;
    public Boolean markReset = false;
    public Ball(TextureRegion region, Float size, Vector2 pos, World world,float restitution) {
        setRegion(region);
        resetState = pos;
        defineBall(pos,world,size,restitution);
        Vector2 position = body.getPosition();
        setPosition(position.x - getWidth() / 2, position.y - getHeight() / 2);
    }
    public Body getBody(){
        return body;
    }
    public void resetBall(){
        body.setTransform(resetState.x, resetState.y, 0);

        // Reset velocity and rotation
        body.setLinearVelocity(Vector2.Zero);
        body.setAngularVelocity(0);

        // Update sprite position
        setPosition(resetState.x - getWidth() / 2, resetState.y - getHeight() / 2);

        // Reset rotation
        setRotation(0);
    }
    private void defineBall(Vector2 pos,World world,Float size,float restitution){
        BodyDef bodyDef = new BodyDef();
// We set our body to dynamic, for something like ground which doesn't move we would set it to StaticBody
        bodyDef.type = BodyDef.BodyType.DynamicBody;
// Set our body's starting position in the world
        bodyDef.position.set(pos);
        bodyDef.linearVelocity.y = 0;
        bodyDef.linearVelocity.x = 0f;

        body = world.createBody(bodyDef);
        body.setBullet(true);

// Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(size / 2f);
        setBounds(pos.x,pos.y,size,size);
        setSize(size,size);
        setOrigin(size/2f,size/2f);
// Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.filter.categoryBits = Bits.BALL_BIT;
        fixtureDef.filter.maskBits = Bits.GROUND_BIT | Bits.GOALSENSOR_BIT | Bits.PLAYER_BIT |Bits.POWERUP_BIT;
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.5f;
        body.setLinearDamping(0.4f);  // Apply linear damping to slow down the ball over time

        fixtureDef.restitution = restitution; // Make it bounce a little bit
        body.createFixture(fixtureDef);
        body.setUserData(this);
    }
    public void setActive(boolean active){
        this.active = active;
    }
    public void updateState(){
        body.setActive(active);

    }
    public void setTrans(Transform transform){
        body.setTransform(transform.getPosition(),transform.getRotation());
    }
    public void update() {
        Vector2 position = body.getPosition();
        setPosition(position.x - getWidth() / 2, position.y - getHeight() / 2);
        setRotation((float) Math.toDegrees(body.getAngle()));
    }

    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }
}
