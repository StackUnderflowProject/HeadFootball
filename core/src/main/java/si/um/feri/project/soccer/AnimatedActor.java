package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.Animation;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class AnimatedActor extends Actor {
    private Animation<TextureRegion> animation;
    private float stateTime; // Tracks the time elapsed for the animation
    private boolean looping;
    private TextureRegion region;

    public AnimatedActor(TextureAtlas atlas,PowerUpType type,String effectType, float frameDuration, boolean looping) {
        // Get the frames from the atlas and create the animation
        this.animation = new Animation<>(frameDuration, atlas.findRegions((type == PowerUpType.NEUTRAL) ? RegionNames.Textures.POWERUPNEUTRAL : ((type == PowerUpType.GOOD) ? RegionNames.Textures.POWERUPGOOD : RegionNames.Textures.POWERUPBAD)));
        this.stateTime = 0f;
        this.looping = looping;
        region = atlas.findRegion(effectType);
        setSize(3*animation.getKeyFrame(0).getRegionWidth()/4f , 3*animation.getKeyFrame(0).getRegionHeight()/4f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta; // Update the animation time;
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        // Get the current frame of the animation
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, looping);

        // Draw the animation frame
        float width = getWidth()*0.3f;
        float height = getHeight()*0.3f;
        batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
        batch.draw(region, getX() + getWidth() / 2- width/2, getY() + getHeight() /2 - height/2, getWidth()*0.3f, getHeight()*0.3f);

    }

    public void resetAnimation() {
        stateTime = 0f; // Reset animation time to the start
    }
}
