package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class GameObject {
    public Sprite sprite;
    public Body body;

    public GameObject(Sprite sprite, Body body) {
        this.sprite = sprite;
        this.body = body;
    }

    public void update() {
        Vector2 position = body.getPosition();
        sprite.setPosition(position.x - sprite.getWidth() / 2, position.y - sprite.getHeight() / 2);
        sprite.setRotation((float) Math.toDegrees(body.getAngle()));
    }
    public void render(SpriteBatch batch){
        sprite.draw(batch);
    }

}

