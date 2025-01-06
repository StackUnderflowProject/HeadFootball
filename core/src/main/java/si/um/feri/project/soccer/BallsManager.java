package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public  class BallsManager {
    private static Array<Ball> balls;
    static private int ballActive;
    static public boolean reset = false;
    static public ID lastTouched = ID.LEFT;
    static public void initialize(TextureAtlas atlas, Vector2 pos, World world){
        balls = new Array<>();
        ballActive = 0;
        balls.add(new Ball(atlas.findRegion(RegionNames.Textures.BALL1),1.5f,pos,world,0.3f));
        balls.add(new Ball(atlas.findRegion(RegionNames.Textures.BOUNCYBALL),1.5f,pos,world,1f));
        balls.add(new Ball(atlas.findRegion(RegionNames.Textures.DULL),1.5f,pos,world,0.01f));
        balls.get(0).setActive(true);
        balls.get(0).updateState();

    }
    static public void update(){
        if(reset){
            resetBalls();
        }
        for(Ball ball : balls){
            ball.update();
            if(!ball.active) {
                ball.setTrans(balls.get(ballActive).getBody().getTransform());
                ball.getBody().setLinearVelocity(balls.get(ballActive).getBody().getLinearVelocity());
            }
            ball.updateState();

        }
    }
    static public void toggleBall(BallType type){
        for(int i = 0;i < balls.size;i++){
            if(i == type.getValue()){
                balls.get(i).setActive(true);
                ballActive = i;
            }
            else{

                balls.get(i).setActive(false);
            }
        }
    }
    static public void activate(){
        for(Ball ball : balls){
            ball.updateState();
        }
    }
    static public void moveInactive(){
        for(Ball ball : balls){
            if(!ball.active) ball.setTrans(balls.get(ballActive).getBody().getTransform());
        }
    }
    static public Vector2 getCurrentBallPosition(){
        return balls.get(ballActive).getBody().getPosition();
    }
    static public void draw(SpriteBatch batch){
        balls.get(ballActive).draw(batch);

    }
    static public void  resetBalls(){

        for (Ball ball : balls){
            ball.resetBall();

        }
        reset = false;
    }
}
