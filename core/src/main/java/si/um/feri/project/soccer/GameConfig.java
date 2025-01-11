package si.um.feri.project.soccer;

public class GameConfig {

    public static final float WIDTH = 640f; // pixels
    public static final float HEIGHT = 360f;    // pixels

    public static final float HUD_WIDTH = 640f; // pixels
    public static final float HUD_HEIGHT = 360;    // pixels

    public static final float WORLD_WIDTH = 64f;    // world units
    public static final float WORLD_HEIGHT = 36f;   // world units
    public static final float GOALWIDTH = 6f;        //meters
    public static final float GOALHEIGHT = 10f;      //meters
    public static final float GROUNDLEVEL = 4f;      //meters
    public static final float MAX_SPEED = 12f;
    public static final float MAX_SPEED_IN_AIR = 10f;

    public static final float MOVE_IMPULSE = 20f;
    public static final float JUMP_IMPULSE = 200f;
    public static final float AI_DESIRED_DISTANCE = 10f;
    public static final float GOOD_MULTI = 1.4f;
    public static final float BAD_MULTI = 0.6f;
    public static final float NEUTRAL_MULTI = 1f;

    public static final float STADUIM_SIZE = 48;
    public static final int DETAILS_WINDOW_WIDTH = 640;
    public static final int DETAILS_WINDOW_HEIGHT = 480;
    public static final int TEAM_LOGO_SIZE = 128;

    private GameConfig() {
    }
}
