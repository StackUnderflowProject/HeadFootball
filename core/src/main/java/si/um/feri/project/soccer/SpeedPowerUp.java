package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class SpeedPowerUp extends PowerUp {
    private TextureRegion effectIcon;
    private boolean activated = false;
    private ID affected;
    private float elapsedTime = 0f;
    public SpeedPowerUp(PowerUpType type, PowerUpEffectType effectType, TextureAtlas atlas, Vector2 pos, World world) {
        super(type, atlas, 3f, 3f, pos, world,effectType);
        this.effectIcon = atlas.findRegion((type == PowerUpType.GOOD) ? RegionNames.Textures.SPEEDGOOD : RegionNames.Textures.SPEEDBAD);
    }

    @Override
    void activate() {
        affected = BallsManager.lastTouched;
        setActivated(true);
        if(super.getEffectType() == PowerUpEffectType.SPEED){
            switch (super.getType()){
                case BAD:
                case GOOD:
                    PlayerManager.setMultiplier(affected,super.getType());
                    break;
                case NEUTRAL:
                    break;
            }
        }
    }
    public void setAffected(ID id){
        this.affected = id;
    }

    @Override
    void deactivate() {
        setActivated(false);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Draw the base power-up first
        super.draw(batch);

        float width = super.getWidth() * 0.3f;
        float heght = super.getWidth() * 0.3f;

        // Calculate the center position of the power-up for the effectIcon
        float iconX = super.getX() +getWidth()/2 - width /2;
        float iconY = super.getY() + getHeight()/2 - heght /2;
        // Draw the effect icon centered over the power-up
        if(!super.isDestroyed())batch.draw(effectIcon, iconX, iconY, width, heght);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(isActivated()){
            elapsedTime += delta;
            if(elapsedTime > 4){
                deactivate();
                destroy();
PlayerManager.unsetMultiplier();
                PowerUpManager.remove(this);
                elapsedTime = 0f;
            }
        }
    }
}
