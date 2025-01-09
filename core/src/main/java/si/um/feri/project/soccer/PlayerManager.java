package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class PlayerManager {
    private static Player player1;
    private static Player player2;


    static public boolean reset = false;

    static public void initialize(Player player11, Player player22){
        player1 = player11;
        player2 = player22;
    }
    static void freezePlayer(ID id){
        if(id == ID.LEFT){
            player1.setFrozen(true);
        }
        else {
            player2.setFrozen(true);
        }
    }
    static void unfreezePlayer(ID id){
        if(id == ID.LEFT){
            player1.setFrozen(false);
        }
        else {
            player2.setFrozen(false);
        }
    }
    static  void  setMultiplier(ID id,PowerUpType type){
        if(id == ID.LEFT){
            player1.setSpeedMultiplier((type == PowerUpType.GOOD ? GameConfig.GOOD_MULTI : GameConfig.BAD_MULTI));
        }
        else {
            player2.setSpeedMultiplier((type == PowerUpType.GOOD ? GameConfig.GOOD_MULTI : GameConfig.BAD_MULTI));

        }
    }
    static  void  unsetMultiplier(){
        player1.setSpeedMultiplier(1);
        player2.setSpeedMultiplier(1);
    }
}
