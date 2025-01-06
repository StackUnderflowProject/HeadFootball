package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class IcePowerUp extends PowerUp {
    private TextureRegion effectIcon;
    private boolean activated = false;
    private ID frozen;
    private float elapsedTime = 0f;
    public IcePowerUp(PowerUpType type,PowerUpEffectType effectType, TextureAtlas atlas, Vector2 pos, World world) {
        super(type, atlas, 3f, 3f, pos, world,effectType);
        this.effectIcon = atlas.findRegion(RegionNames.Textures.ICE);
    }

    @Override
    void activate() {
        frozen = BallsManager.lastTouched;
        activated = true;
        if(super.getEffectType() == PowerUpEffectType.ICE){
            switch (super.getType()){
                case BAD:
                    PlayerManager.freezePlayer(frozen);
                    break;
                case GOOD:
                    PlayerManager.freezePlayer(((frozen == ID.LEFT) ? ID.RIGHT : ID.LEFT));
                    break;
                case NEUTRAL:
                    PlayerManager.freezePlayer(ID.LEFT);
                    PlayerManager.freezePlayer(ID.RIGHT);

            }
        }
    }
    public void setFrozen(ID id){
        this.frozen = id;
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
        float iconX = super.getX() + getWidth()/2 - width /2;
        float iconY = super.getY() + getHeight()/2 - heght /2;
        // Draw the effect icon centered over the power-up
        if(!super.isDestroyed())batch.draw(effectIcon, iconX, iconY, width, heght);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if(activated){
            System.out.println("Destoy");

            elapsedTime += delta;
            if(elapsedTime > 5){
                deactivate();
                destroy();
                if(getType() == PowerUpType.NEUTRAL){
                    PlayerManager.unfreezePlayer(ID.RIGHT);
                    PlayerManager.unfreezePlayer(ID.LEFT);
                } else if (getType() == PowerUpType.GOOD) {
                    PlayerManager.unfreezePlayer((ID.LEFT == frozen) ? ID.RIGHT : ID.LEFT);

                } else{
                    PlayerManager.unfreezePlayer(frozen);

                }
                PowerUpManager.remove(this);
                elapsedTime = 0f;
            }
        }
    }
}
