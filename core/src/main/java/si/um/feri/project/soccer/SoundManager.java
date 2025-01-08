package si.um.feri.project.soccer;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    static public Sound jump;
    static public Sound cheer;
    static public Sound coin;
    static public Sound pop;
    static  public void init(AssetManager manager){
        jump = manager.get(AssetDescriptors.JUMP);
        cheer = manager.get(AssetDescriptors.CHEER);
        coin = manager.get(AssetDescriptors.COIN);
        pop = manager.get(AssetDescriptors.POP);
    }
}
