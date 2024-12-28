package si.um.feri.project.soccer;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;


public class AssetDescriptors {


    public static final AssetDescriptor<TextureAtlas> GAMEPLAY =
        new AssetDescriptor<TextureAtlas>(AssetsPaths.GAMEPLAY, TextureAtlas.class);
    public static final AssetDescriptor<BitmapFont> TITLE_FONT =
        new AssetDescriptor<>(AssetsPaths.TITLE_FONT,BitmapFont.class);

    public static final AssetDescriptor<BitmapFont> FONT =
        new AssetDescriptor<>(AssetsPaths.FONT,BitmapFont.class);
    public static final AssetDescriptor<Skin> UI_SKIN = new AssetDescriptor<>(AssetsPaths.SKIN,Skin.class);

    private AssetDescriptors() {
    }
}
