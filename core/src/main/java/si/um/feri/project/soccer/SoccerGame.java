package si.um.feri.project.soccer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Logger;

import java.util.LinkedHashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SoccerGame extends Game {
    public AssetManager getAssetManager() {
        return assetManager;
    }

    private AssetManager assetManager;
    public Music music;
    public Long musicID;

    public SpriteBatch getBatch() {
        return batch;
    }

    private SpriteBatch batch;
    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_ERROR);
        assetManager = new AssetManager();
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.load(AssetDescriptors.TITLE_FONT);
        assetManager.load(AssetDescriptors.FONT);
        assetManager.load(AssetDescriptors.FONT1);
        assetManager.load(AssetDescriptors.FONT2);
        assetManager.load(AssetDescriptors.GAMEOVER);

        assetManager.finishLoading();

        assetManager.getLogger().setLevel(Logger.ERROR);
        TextureAtlas atlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        batch = new SpriteBatch();
        String t1 = "Maribor";
        String t2 = "Olimpija";
        System.out.println(t1.toLowerCase() + ".p");
        setScreen(new SelectionScreen(this,new Team(t1,atlas.findRegion(t1.toLowerCase()),atlas.findRegion(t1.toLowerCase() + "p")),new Team(t2,atlas.findRegion(t2.toLowerCase()),atlas.findRegion(t2.toLowerCase() + "p")),Mode.LOCALMULTIPLAYER));


    }
}
