package si.um.feri.project.soccer;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.awt.Menu;


public class InstructionsScreen extends ScreenAdapter {

    private final SoccerGame game;
    private final AssetManager assetManager;

    private Viewport viewport;
    private Stage stage;
    private Skin skin;

    private TextureAtlas gameplayAtlas;

    public InstructionsScreen(SoccerGame game) {
        this.game = game;
        viewport = new StretchViewport(GameConfig.HUD_WIDTH,GameConfig.HUD_HEIGHT);
        stage = new Stage(viewport, game.getBatch());
        Gdx.input.setInputProcessor(stage);
        assetManager = game.getAssetManager();

    }
    public Pixmap extractPixmapFromTextureRegion(TextureRegion textureRegion) {
        TextureData textureData = textureRegion.getTexture().getTextureData();
        if (!textureData.isPrepared()) {
            textureData.prepare();
        }
        Pixmap pixmap = new Pixmap(
            textureRegion.getRegionWidth(),
            textureRegion.getRegionHeight(),
            textureData.getFormat()
        );
        pixmap.drawPixmap(
            textureData.consumePixmap(), // The other Pixmap
            0, // The target x-coordinate (top left corner)
            0, // The target y-coordinate (top left corner)
            textureRegion.getRegionX(), // The source x-coordinate (top left corner)
            textureRegion.getRegionY(), // The source y-coordinate (top left corner)
            textureRegion.getRegionWidth(), // The width of the area from the other Pixmap in pixels
            textureRegion.getRegionHeight() // The height of the area from the other Pixmap in pixels
        );
        return pixmap;
    }

    private Pixmap applyMask(Pixmap source) {
        /* Create a Pixmap to store the mask information, at the end it will
         * contain the result. */
        Pixmap result = new Pixmap(source.getWidth(), source.getHeight(), Pixmap.Format.RGBA8888);

        /* This setting lets us overwrite the pixels' transparency. */
        result.setBlending(Pixmap.Blending.None);

        /* Ignore RGB values unless you want funky results, alpha is for the mask. */
        result.setColor(new Color(1f, 1f, 1f, 1f));

        /* Draw a circle to our mask, any shape is possible since
         * you can draw individual pixels to the Pixmap. */
        result.fillCircle(source.getWidth() / 2, source.getHeight() / 2, source.getHeight() / 2);



        /* We can also define the mask by loading an image:
         * result = new Pixmap(new FileHandle("image.png")); */

        /* Decide the color of each pixel using the AND bitwise operator. */
        for (int x = 0; x < result.getWidth(); x++) {
            for (int y = 0; y < result.getHeight(); y++) {
                result.drawPixel(x, y, source.getPixel(x, y) & result.getPixel(x, y));
            }
        }

        return result;
    }
    @Override
    public void show() {
        skin = assetManager.get(AssetDescriptors.UI_SKIN);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();
        rootTable.setBackground(new TextureRegionDrawable(gameplayAtlas.findRegion("t")));
        stage.addActor(rootTable);

        BitmapFont font = assetManager.get(AssetDescriptors.INFO);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;

        // Create AnimatedActors
        AnimatedActor animatedActor = new AnimatedActor(gameplayAtlas, PowerUpType.GOOD, RegionNames.Textures.SPEEDGOOD, 0.05f, true);
        AnimatedActor animatedActor1 = new AnimatedActor(gameplayAtlas, PowerUpType.BAD, RegionNames.Textures.SPEEDBAD, 0.05f, true);
        AnimatedActor ballDull = new AnimatedActor(gameplayAtlas, PowerUpType.NEUTRAL, RegionNames.Textures.DULL, 0.05f, true);
        AnimatedActor ballBouncy = new AnimatedActor(gameplayAtlas, PowerUpType.NEUTRAL, RegionNames.Textures.BOUNCYBALL, 0.05f, true);
        AnimatedActor goalSmall = new AnimatedActor(gameplayAtlas, PowerUpType.BAD, RegionNames.Textures.BIG, 0.05f, true);
        AnimatedActor goalBig = new AnimatedActor(gameplayAtlas, PowerUpType.GOOD, RegionNames.Textures.BIG, 0.05f, true);
        AnimatedActor goalNormal = new AnimatedActor(gameplayAtlas, PowerUpType.NEUTRAL, RegionNames.Textures.BIG, 0.05f, true);
        AnimatedActor freezeGood = new AnimatedActor(gameplayAtlas, PowerUpType.GOOD, RegionNames.Textures.ICE, 0.05f, true);
        AnimatedActor freezeBad = new AnimatedActor(gameplayAtlas, PowerUpType.BAD, RegionNames.Textures.ICE, 0.05f, true);

        // Title label
        rootTable.defaults().padBottom(30);
        rootTable.add(new Label("Instructions", labelStyle)).colspan(3).padBottom(10).row();

        rootTable.debug(); // This is for debugging the layout

        // First row: Speed power-up
        Table cellTable1 = new Table();
        cellTable1.add(animatedActor).padRight(10); // Add animated actor
        cellTable1.add(new Label("Gain Speed", skin)).left(); // Add label next to actor
        rootTable.add(cellTable1).left(); // Add to root table and set padding

        // Second row: Lose speed
        Table cellTable2 = new Table();
        cellTable2.add(animatedActor1).padRight(10); // Add animated actor
        cellTable2.add(new Label("Lose Speed", skin)).left(); // Add label next to actor
        rootTable.add(cellTable2).left(); // Add to root table

        // Third row: Ball dull
        Table cellTable3 = new Table();
        cellTable3.add(ballDull).padRight(10); // Add animated actor
        cellTable3.add(new Label("Ball loses bounciness", skin)).left(); // Add label
        rootTable.add(cellTable3).left(); // Add to root table
        rootTable.row(); // Next row

        // Fourth row: Ball bouncy
        Table cellTable4 = new Table();
        cellTable4.add(ballBouncy).padRight(10); // Add animated actor
        cellTable4.add(new Label("Ball becomes bouncy", skin)).left(); // Add label
        rootTable.add(cellTable4).left(); // Add to root table

        // Fifth row: Enemy goal shrinks
        Table cellTable5 = new Table();
        cellTable5.add(goalSmall).padRight(10); // Add animated actor
        cellTable5.add(new Label("Enemy goal shrinks", skin)).left(); // Add label
        rootTable.add(cellTable5).left(); // Add to root table

        // Sixth row: Enemy goal grows
        Table cellTable6 = new Table();
        cellTable6.add(goalBig).padRight(10); // Add animated actor
        cellTable6.add(new Label("Enemy goal grows", skin)).left(); // Add label
        rootTable.add(cellTable6).left(); // Add to root table
        rootTable.row(); // Next row

        // Seventh row: Both goals grow
        Table cellTable7 = new Table();
        cellTable7.add(goalNormal).padRight(10); // Add animated actor
        cellTable7.add(new Label("Both goals grow", skin)).left(); // Add label
        rootTable.add(cellTable7).left(); // Add to root table

        // Eighth row: Enemy freezes
        Table cellTable8 = new Table();
        cellTable8.add(freezeGood).padRight(10); // Add animated actor
        cellTable8.add(new Label("Enemy freezes", skin)).left(); // Add label
        rootTable.add(cellTable8).left(); // Add to root table

        // Ninth row: You freeze
        Table cellTable9 = new Table();
        cellTable9.add(freezeBad).padRight(10); // Add animated actor
        cellTable9.add(new Label("You freeze", skin)).left(); // Add label
        rootTable.add(cellTable9).left(); // Add to root table
        rootTable.row(); // Next row

        font.getData().setScale(0.7f);
        // Create ImageButton for single player
        TextButton.TextButtonStyle singleplayerStyle = new TextButton.TextButtonStyle();
        singleplayerStyle.font = font; // Set your desired font here

        singleplayerStyle.up = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.SINGLEPLAYER)); // Normal state
        singleplayerStyle.down = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.SINGLEPLAYER)); // Pressed state
        singleplayerStyle.over = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.SINGLEPLAYER)); // Hover state (optional)

        TextButton multiplayer = new TextButton("Back", singleplayerStyle);
        multiplayer.setTransform(true); // Enable transformations like scaling
        multiplayer.setOrigin(Align.center);
        multiplayer.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.getPrevScreen());
                float randomPitch = 0.8f + (float) Math.random() * 0.4f;

                SoundManager.coin.play(0.5f,randomPitch,0f);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                multiplayer.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f)); // Scale up to 120% over 0.1 seconds
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                multiplayer.addAction(Actions.scaleTo(1f, 1f, 0.1f)); // Scale up to 120% over 0.1 seconds

            }
        });
        multiplayer.pad(0,10,0,10);
        rootTable.add(multiplayer).colspan(3);
    }



    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 0f);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

}
