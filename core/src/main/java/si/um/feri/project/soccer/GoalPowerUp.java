package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class GoalPowerUp extends PowerUp {
    private TextureRegion effectIcon;
    private float elapsedTime = 0f;
    public GoalPowerUp(PowerUpType type,PowerUpEffectType effectType, TextureAtlas atlas, Vector2 pos, World world) {
        super(type, atlas, 3f, 3f, pos, world,effectType);
        this.effectIcon = atlas.findRegion(RegionNames.Textures.SMALL);
        this.effectIcon.flip(true,false);
    }

    @Override
    void activate() {
        setActivated(true);
        switch (super.getEffectType()){
            case GOALBIG:
                GoalManager.toggleGoal(GoalType.BIG,BallsManager.lastTouched);
                break;
            case GOALMEDIUM:
                GoalManager.toggleGoal(GoalType.BIG,ID.RIGHT);
                GoalManager.toggleGoal(GoalType.BIG,ID.LEFT);
                break;
            case GOALSMALL:
                GoalManager.toggleGoal(GoalType.SMALL,BallsManager.lastTouched);
                break;
            default:
                System.out.println("Wrong type");
        }
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
        if(isActivated()){
            elapsedTime += delta;
            if(elapsedTime > 3){
                deactivate();
                destroy();
                GoalManager.toggleGoal(GoalType.NORMAL,ID.RIGHT);
                GoalManager.toggleGoal(GoalType.NORMAL,ID.LEFT);

                PowerUpManager.remove(this);
                elapsedTime = 0f;
            }
        }
    }
}
