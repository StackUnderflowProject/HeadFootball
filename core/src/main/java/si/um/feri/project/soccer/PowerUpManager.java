package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

public  class PowerUpManager {
    private static Array<PowerUp> active;
    private static Viewport viewport1;
    private static TextureAtlas atlas1;
    private static World world1;
    private static float spawnTimer = 0f;  // Timer to track the spawn interval
    private static final float SPAWNTIME = 5f;

    public static boolean isToClear() {
        return toClear;
    }

    public static void setToClear(boolean toClear) {
        PowerUpManager.toClear = toClear;
    }

    private static boolean toClear = false;

    static public void initialize(TextureAtlas atlas, Viewport viewport, World world){
        active = new Array<>();
        viewport1 = viewport;
        atlas1 = atlas;
        world1 = world;
        active.add(new BallPowerUp(PowerUpEffectType.BALLDULL,atlas,new Vector2(viewport.getWorldWidth() / 2 ,viewport.getWorldHeight() / 2),world));

    }
    private static void spawn(){
        float randomX = (float) Math.random() * viewport1.getWorldWidth();
        randomX = Math.max(GameConfig.GOALWIDTH * 1.5f,randomX);
        randomX = Math.min(viewport1.getWorldWidth() - GameConfig.GOALWIDTH * 1.5f,randomX);

        float randomY = (float) Math.random() * (viewport1.getWorldHeight()/2);
        randomY = Math.max(GameConfig.GROUNDLEVEL,4);

        PowerUpType randomType = PowerUpType.values()[(int) (Math.random() * PowerUpType.values().length)];

        PowerUpEffectType randomEffect = PowerUpEffectType.values()[(int) (Math.random() * PowerUpEffectType.values().length)];
        switch (randomEffect){
            case BALLBOUNCY:
            case BALLDULL:
                active.add(new BallPowerUp(randomEffect,atlas1,new Vector2(randomX ,randomY),world1));
                break;
            case GOALSMALL:
                active.add(new GoalPowerUp(PowerUpType.BAD,randomEffect,atlas1,new Vector2(randomX,randomY),world1));
                break;

            case GOALMEDIUM:
                active.add(new GoalPowerUp(PowerUpType.NEUTRAL,randomEffect,atlas1,new Vector2(randomX,randomY),world1));
                break;

            case GOALBIG:
                active.add(new GoalPowerUp(PowerUpType.GOOD,randomEffect,atlas1,new Vector2(randomX,randomY),world1));
                break;
            case ICE:
                active.add(new IcePowerUp(randomType,randomEffect,atlas1,new Vector2(randomX,randomY),world1));
        }

    }
    static public void update(float delta){
        spawnTimer += delta;  // Increment the timer by the delta time
        if (spawnTimer >= SPAWNTIME) {  // If 3 seconds have passed
            if (active.size <= 3){
                spawn();  // Call spawn method
            }
            spawnTimer = 0f;  // Reset the timer
        }
        for(PowerUp powerUp : active){

                powerUp.update(delta);
        }


    }
    static public void move(PowerUp p ){
        active.removeValue(p,true);
    }
    static public void remove(PowerUp p ){
        active.removeValue(p,true);
    }

    static public void draw(SpriteBatch batch){
        for(PowerUp p : active){
            if(!p.isDestroyed()){
                p.draw(batch);
            }
        }
    }
    static public void clear() {
        // Mark all active power-ups as destroyed and activated
        for (PowerUp p : active) {
            p.destroy();  // Mark the power-up to be destroyed
        }

        active.clear();  // Clear the array of active power-ups
        toClear = false;
    }

}
