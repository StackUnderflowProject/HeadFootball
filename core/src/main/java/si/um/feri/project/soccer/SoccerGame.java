package si.um.feri.project.soccer;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Logger;

import java.util.LinkedHashMap;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class SoccerGame extends Game {
    public AssetManager getAssetManager() {
        return assetManager;
    }

    static public AssetManager assetManager;
    static public Music music;


    public Screen getPrevScreen() {
        return prevScreen;
    }

    public void setPrevScreen(Screen prevScreen) {
        this.prevScreen = prevScreen;
    }

    private Screen prevScreen;
    public Long musicID;

    @Override
    public void setScreen(Screen screen) {
        if (this.getScreen() != null) {
            // Store the current screen as previous
            setPrevScreen(this.getScreen());
            System.out.println("Null screen");
        }
        super.setScreen(screen);
    }
    public SpriteBatch getBatch() {
        return batch;
    }

    private SpriteBatch batch;
    static public void loadMusic(){
music.setVolume(GamePreferences.loadMusicVolume());    }
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
        assetManager.load(AssetDescriptors.INFO);
        assetManager.load(AssetDescriptors.INFOGREEN);
assetManager.load(AssetDescriptors.LOOP);
assetManager.load(AssetDescriptors.WHISTLE);
        assetManager.load(AssetDescriptors.JUMP);
        assetManager.load(AssetDescriptors.POP);
        assetManager.load(AssetDescriptors.CHEER);
        assetManager.load(AssetDescriptors.COIN);


        assetManager.finishLoading();
        SoundManager.init(assetManager);
        assetManager.getLogger().setLevel(Logger.ERROR);
        TextureAtlas atlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        music = assetManager.get(AssetDescriptors.LOOP);
        music.setVolume(GamePreferences.loadMusicVolume());
        music.setLooping(true);
        music.play();

        batch = new SpriteBatch();
        String t1 = "Maribor";
        String t2 = "Koper";
        System.out.println(t1.toLowerCase() + ".p");
       // setScreen(new InstructionsScreen(this));
        setScreen(new MenuScreen(this,new Team(t1,atlas.findRegion(t1.toLowerCase()),atlas.findRegion(t1.toLowerCase() + "p")),new Team(t2,atlas.findRegion(t2.toLowerCase()),atlas.findRegion(t2.toLowerCase() + "p")),Mode.SINGLEPLAYER));
        //setScreen(new GameOverScreen(this,new Team(t1,atlas.findRegion(t1.toLowerCase()),atlas.findRegion(t1.toLowerCase() + "p")),new Team(t2,atlas.findRegion(t2.toLowerCase()),atlas.findRegion(t2.toLowerCase() + "p")),new Player(atlas.findRegion(t2.toLowerCase()),atlas.findRegion(RegionNames.Textures.ICE),10,10,new Vector2(),new World(new Vector2(),false),ID.LEFT,1,1,1),new Player(atlas.findRegion(t2.toLowerCase()),atlas.findRegion(RegionNames.Textures.ICE),100,100,new Vector2(0,0),new World(new Vector2(0,0),false),ID.RIGHT,1,1,1)));
        //setScreen(new SelectionScreen(this,new Team(t1,atlas.findRegion(t1.toLowerCase()),atlas.findRegion(t1.toLowerCase() + "p")),new Team(t2,atlas.findRegion(t2.toLowerCase()),atlas.findRegion(t2.toLowerCase() + "p")),Mode.SINGLEPLAYER));


    }
}
