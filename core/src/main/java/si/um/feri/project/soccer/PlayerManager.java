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
}
