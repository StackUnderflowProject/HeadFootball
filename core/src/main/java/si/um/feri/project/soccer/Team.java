package si.um.feri.project.soccer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Team {

    private String name;
    private TextureRegion textureRegion;

    public TextureRegion getPlayer() {
        return player;
    }

    private TextureRegion player;

    // Constructor
    public Team(String name, TextureRegion textureRegion,TextureRegion p) {
        this.name = name;
        this.textureRegion = textureRegion;
        this.player = p;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    // Getter for textureRegion
    public TextureRegion getTextureRegion() {
        return textureRegion;
    }

    // Setter for textureRegion
    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    @Override
    public String toString() {
        return "Team{" +
            "name='" + name + '\'' +
            ", textureRegion=" + (textureRegion != null ? "TextureRegion set" : "No TextureRegion") +
            '}';
    }
}
