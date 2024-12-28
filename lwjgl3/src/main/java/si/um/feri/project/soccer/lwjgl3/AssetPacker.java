package si.um.feri.project.soccer.lwjgl3;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class AssetPacker {


    private static final String RAW_ASSETS_PATH = "assets/assets-raw";
    private static final String ASSETS_PATH = "assets/assets";

    public static void main(String[] args) {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 32768;
        settings.maxHeight = 32768;
        TexturePacker.process(settings,
            RAW_ASSETS_PATH ,   // the directory containing individual images to be packed
            ASSETS_PATH,   // the directory where the pack file will be written
            "gameplay"   // the name of the pack file / atlas name
        );
    }
}
