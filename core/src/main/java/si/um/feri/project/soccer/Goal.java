package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Goal extends Sprite {

    private TextureRegion texture;
    private Body body;
    public ID id;
    public Goal(TextureRegion region, float width,float height, Vector2 pos, World world,ID id) {
        setRegion(region);
        this.id = id;
        setSize(width, height);
        setPosition(pos.x, pos.y);
        defineGoal(pos,world,width,height);
    }
    private void defineGoal(Vector2 pos,World world,Float width,Float height){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);

        System.out.println("Goal ID: " + id);

        EdgeShape edgeShape = new EdgeShape();

// Bottom edge
        edgeShape.set(pos.x, pos.y, pos.x + width, pos.y);

        body.createFixture(edgeShape, 0);

  edgeShape.set(pos.x, pos.y + height, pos.x + width, pos.y + height);
  if(ID.GOal2 == id){
      edgeShape.set(pos.x, pos.y + height, pos.x + width, pos.y + height + 2);

  }
  else{
      edgeShape.set(pos.x, pos.y + height + 2, pos.x + width, pos.y + height);

  }
  FixtureDef topFix = new FixtureDef();
  topFix.shape = edgeShape;
  topFix.filter.categoryBits = Bits.GROUND_BIT;  // Make sure top edge is categorized differently if needed
  topFix.filter.maskBits = Bits.BALL_BIT;  // Make sure the ball interacts with the top edge (if necessary)
   body.createFixture(topFix);

// Left edge
        edgeShape.set(pos.x,pos.y,pos.x, pos.y + height - 2);
        FixtureDef leftFix = new FixtureDef();
        leftFix.shape = edgeShape;
        leftFix.filter.categoryBits = (ID.GOal2 == id) ? Bits.GOALSENSOR_BIT :  Bits.GROUND_BIT;
        leftFix.filter.maskBits = Bits.BALL_BIT; // Define specific maskBits if needed
        leftFix.isSensor = id == ID.GOAL1;; // Sensor based on id
        body.createFixture(leftFix);

// Right edge
        edgeShape.set(pos.x + width, pos.y, pos.x + width, pos.y + height - 2);
        FixtureDef rightFix = new FixtureDef();
        rightFix.shape = edgeShape;
        rightFix.filter.categoryBits = (ID.GOAL1 == id) ? Bits.GOALSENSOR_BIT :  Bits.GROUND_BIT;
        rightFix.filter.maskBits = Bits.BALL_BIT; // Interacts only with category 2
        rightFix.isSensor = id == ID.GOal2; // Sensor based on id
        body.createFixture(rightFix);
        body.setUserData(this);
// Dispose of the shape when done
        edgeShape.dispose();
    }



    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }
}
