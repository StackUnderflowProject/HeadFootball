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

    private GameConfig() {
    }
}
