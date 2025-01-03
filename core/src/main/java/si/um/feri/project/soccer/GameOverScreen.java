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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
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
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


public class GameOverScreen extends ScreenAdapter {

    private final SoccerGame game;
    private final AssetManager assetManager;

    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Team team1;
    private Team team2;
    private Player player1;
    private Player player2;

    private TextureAtlas gameplayAtlas;

    public GameOverScreen(SoccerGame game,Team team1,Team team2,Player pl1,Player pl2) {
        this.game = game;
        viewport = new StretchViewport(GameConfig.HUD_WIDTH,GameConfig.HUD_HEIGHT);
        stage = new Stage(viewport, game.getBatch());
        assetManager = game.getAssetManager();
        this.team1 = team1;
        this.team2 = team2;
        this.player1 = pl1;
        this.player2 = pl2;
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
        rootTable.setBackground(new TextureRegionDrawable(gameplayAtlas.findRegion("t")));
        stage.addActor(rootTable);
        skin.getFont("font-label").setColor(Color.BLACK);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = assetManager.get(AssetDescriptors.TITLE_FONT);
        //  labelStyle.fontColor = Color.BLACK;


// Create a container for the labels
        Table labelTable = new Table(); // Debug for layout visualization
        labelTable.pad(0); // Remove extra padding

// Load photos (textures)
        TextureRegionDrawable photo1 = new TextureRegionDrawable(team1.getTextureRegion()); // Team 1 photo
        TextureRegionDrawable photo2 = new TextureRegionDrawable(team2.getTextureRegion()); // Team 2 photo

// Create Image objects for the photos
        Pixmap pixmap = extractPixmapFromTextureRegion(team1.getTextureRegion());


        pixmap = applyMask(pixmap);

        /* Load the pixel information of the Pixmap into a Texture for drawing. */
        Texture masked = new Texture(pixmap);
        Image photo1Image = new Image(masked);
        pixmap = extractPixmapFromTextureRegion(team2.getTextureRegion());


        pixmap = applyMask(pixmap);

        /* Load the pixel information of the Pixmap into a Texture for drawing. */
        masked = new Texture(pixmap);
        Image photo2Image = new Image(masked);

// Create a table for Team 1
        Table teamTable = new Table();
        //labelStyle.font.getData().setScale(0.3f); // Reduce the font size by adjusting the scale
        float imageSize = Math.min(viewport.getWorldWidth(), viewport.getWorldHeight()) * 0.2f;
        teamTable.add(photo1Image).pad(5).size(imageSize, imageSize).row();

        //teamTable.add(photo1Image).pad(5).size(50, 50).row(); // Photo in the first column

        String result1 = (player1.getScore() > player2.getScore())
            ? "Winner"
            :  "Loser";
        Label score2label = new Label(result1,labelStyle);
        score2label.setColor(   result1.equals("Winner") ? Color.GREEN : Color.RED);
        teamTable.add(score2label);
        //teamTable.add(new Label(team1.getName(), labelStyle)).padTop(5); // Team name in the second column

// Create a table for the time
        Pixmap pixmap1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap1.setColor(Color.valueOf("210555" )); // Set color #3A3960
        pixmap1.fill();

// Create a Texture from the Pixmap
        Texture texture = new Texture(pixmap1);

// Create a Drawable from the Texture
        Drawable colorBackground = new TextureRegionDrawable(texture);

        Table teamTable1 = new Table();
        teamTable1.add(photo2Image).pad(5).size(imageSize, imageSize).row(); // Photo in the first column

        String result = (player2.getScore() > player1.getScore())
            ? "Winner"
            :  "Loser";
        Label score1label = new Label(result,labelStyle);
        score1label.setColor(   result.equals("Winner") ? Color.GREEN :  Color.RED);
        teamTable1.add(score1label);
        //teamTable1.add(new Label(team2.getName(), labelStyle)).padTop(5);
// Add the teamTable and timeTable to the labelTable
        // Team 2 photo in the third column
        Table scoreTable = new Table();
        labelTable.setBackground(colorBackground);
        pixmap1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap1.setColor(Color.valueOf("3E5879")); // Set color #3A3960
        pixmap1.fill();

// Create a Texture from the Pixmap
        texture = new Texture(pixmap1);

// Create a Drawable from the Texture
        colorBackground = new TextureRegionDrawable(texture);

        Label score1 = new Label(String.valueOf(player1.getScore()),labelStyle);
        Label score2 = new Label(String.valueOf(player2.getScore()),labelStyle);;
        scoreTable.add(score1).colspan(2).pad(0); // Takes 1 column

        scoreTable.add(score2).colspan(2).pad(0).row(); // Takes 1 column
        scoreTable.add(new Label(team1.getName(), labelStyle)).colspan(1).pad(5);
        //scoreTable.add(new Label("vs",labelStyle)).colspan(2);
        scoreTable.add(new Image(new TextureRegionDrawable(gameplayAtlas.findRegion("vs")))).size(20,20).colspan(2);

        scoreTable.add(new Label(team2.getName(), labelStyle)).colspan(1).pad(5);

        labelTable.add(teamTable).pad(5); // Team info in the first column
        labelTable.add(scoreTable).pad(5).padLeft(10).padRight(10); // Time info in the second column
        labelTable.add(teamTable1).pad(5);

// Optional: Center the labelTable
        labelTable.center();

// Add the label table to the root table at the top with minimal padding
        rootTable.add(labelTable).top().padTop(10).padLeft(1).padRight(1);

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
