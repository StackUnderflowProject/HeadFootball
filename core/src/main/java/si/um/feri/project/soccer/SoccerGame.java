package si.um.feri.project.soccer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
        assetManager.finishLoading();

        assetManager.getLogger().setLevel(Logger.ERROR);

        batch = new SpriteBatch();

        setScreen(new SelectionScreen(this,new Team("Maribor",assetManager.get(AssetDescriptors.GAMEPLAY).findRegion(RegionNames.Textures.MARIBOR)),new Team("Olimpija",assetManager.get(AssetDescriptors.GAMEPLAY).findRegion(RegionNames.Textures.OLIMPIJA)),Mode.LOCALMULTIPLAYER));


    }
}
