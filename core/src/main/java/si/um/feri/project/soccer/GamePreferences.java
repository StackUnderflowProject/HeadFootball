package si.um.feri.project.soccer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class GamePreferences {

    private static Preferences prefs;

    static {
        prefs = Gdx.app.getPreferences("GameSettings"); // Name of the preferences file
    }
    public static float loadSoundVolume() {
        return prefs.getFloat("soundVolume");
    }
    public static void setSoundVolume(float volume) {
        prefs.putFloat("soundVolume",volume);
        prefs.flush();
    }

    public static void toggleMusic() {
        if(loadMusicVolume() == 1f){
            prefs.putFloat("musicVolume",0f);

        }
        else{
            prefs.putFloat("musicVolume",1);

        }
        prefs.flush();
    }

    public static float loadMusicVolume() {
        return prefs.getFloat("musicVolume");
    }
}
