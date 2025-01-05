package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class GoalManager {
    private static Array<Goal> goals1;
    private static Array<Goal> goals2;

    static private int goalActive1;
    static private int goalActive2;

    static public boolean reset = false;

    static public void initialize(TextureAtlas atlas, World world,float worldWidth){
        goals1 = new Array<>();
        goals2 = new Array<>();
        goalActive1 = 1;
        goalActive2 = 1;
        for(int i = 0;i < 3;i++){
            float width = GameConfig.GOALWIDTH + i * 1f;
            float height = GameConfig.GOALHEIGHT + i * 2f;
            goals1.add(new Goal(atlas.findRegion(GoalType.valueOf(i).getRegionname()),width,height,new Vector2(0,GameConfig.GROUNDLEVEL),world,ID.LEFT));
            goals2.add(new Goal(atlas.findRegion(GoalType.valueOf(i).getRegionname()),width,height,new Vector2(worldWidth - width,GameConfig.GROUNDLEVEL),world,ID.RIGHT));

        }
        goals1.get(goalActive1).setActive(true);
        goals2.get(goalActive2).setActive(true);

        goals1.get(goalActive1).updateState();
        goals2.get(goalActive2).updateState();


    }
    static public void update(){

        for(int i = 0; i < goals1.size; i++){
            goals1.get(i).updateState();
            goals2.get(i).updateState();
        }
    }
    static public void toggleGoal(GoalType type,ID id){
        for(int i = 0; i < goals1.size; i++){
            if(i == type.getValue()){
                if(id == ID.LEFT){
                    goals1.get(i).setActive(true);
                    goalActive1 = i;

                }
                else {
                    goals2.get(i).setActive(true);
                    goalActive2 = i;

                }
            }
            else{
                if(id == ID.LEFT){
                    goals1.get(i).setActive(false);

                }
                else {
                    goals2.get(i).setActive(false);
                }
            }
        }
    }


    static public void draw(SpriteBatch batch){
        goals1.get(goalActive1).draw(batch);
        goals2.get(goalActive2).draw(batch);

    }
}
