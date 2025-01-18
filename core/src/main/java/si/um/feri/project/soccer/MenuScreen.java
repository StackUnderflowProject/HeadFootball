package si.um.feri.project.soccer;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.KnownFonts;
import com.github.tommyettinger.textra.TextraLabel;


public class MenuScreen extends ScreenAdapter {

    private final SoccerGame game;
    private final AssetManager assetManager;

    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private TextureAtlas gameplayAtlas;
    private Label label;
    private Team team1, team2;
    private Mode mode;
    private ParticleEffect particleEffect1 = new ParticleEffect();
    private ParticleEffect particleEffect2 = new ParticleEffect();


    public MenuScreen(SoccerGame game, Team team1, Team team2, Mode mode) {
        this.game = game;
        viewport = new StretchViewport(GameConfig.HUD_WIDTH, GameConfig.HUD_HEIGHT);

        stage = new Stage(viewport, game.getBatch());
        this.particleEffect1.load(Gdx.files.internal("jump.p"), Gdx.files.internal("") // Directory containing particle textures
        );
        this.particleEffect2.load(Gdx.files.internal("jump.p"), Gdx.files.internal("")); // Directory containing particle textures
        particleEffect2.scaleEffect(3f);
        particleEffect1.scaleEffect(3f);
        this.team1 = team1;
        this.team2 = team2;
        this.mode = mode;
        assetManager = game.getAssetManager();
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);


    }

    @Override
    public void show() {

        skin = assetManager.get(AssetDescriptors.UI_SKIN);
        stage.addAction(Actions.sequence(Actions.alpha(0.5f), Actions.fadeIn(0.2f)));

        stage.addActor(new Image(gameplayAtlas.findRegion(RegionNames.Textures.FIELD)));
        ImageButton.ImageButtonStyle musicButtonStyle = new ImageButton.ImageButtonStyle();

        musicButtonStyle.up = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.ON)); // Music on texture
        musicButtonStyle.checked = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.OFF)); // Music off texture

        ImageButton musicButton = new ImageButton(musicButtonStyle);

        musicButton.setChecked(GamePreferences.loadMusicVolume() == 0);
        musicButton.setTransform(true);
        musicButton.setOrigin(Align.center);
        musicButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GamePreferences.toggleMusic(); // Assuming you have a method in SoccerGame for toggling music
                SoccerGame.loadMusic();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                musicButton.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f)); // Scale up to 120% over 0.1 seconds
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                musicButton.addAction(Actions.scaleTo(1f, 1f, 0.1f)); // Scale back to normal
            }
        });

        ;
        Table layoutTable = new Table();
        layoutTable.setFillParent(true); // Make the table span the entire screen

        // Add the music button to the top-right
        layoutTable.top().right().add(musicButton).pad(10);
        stage.addActor(layoutTable);
        stage.addActor(createUi());

        stage.addActor(createAnimatedBall(new Vector2(viewport.getWorldWidth() / 6f, viewport.getWorldHeight() / 5.7f), 200f, particleEffect1));
        stage.addActor(createAnimatedBall(new Vector2(viewport.getWorldWidth() - viewport.getWorldWidth() / 6f, viewport.getWorldHeight() / 5.7f), 200f, particleEffect2)); // Add the animated ball to the stage

        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private Actor createAnimatedBall(Vector2 pos, float amount, ParticleEffect particleEffect) {
        // Load the ball texture region
        TextureRegion ballRegion = gameplayAtlas.findRegion(RegionNames.Textures.BALL1);
        Image ball = new Image(new TextureRegionDrawable(ballRegion));

        // Set the initial position and size of the ball
        ball.setSize(20, 20); // Set size (adjust as needed)
        ball.setPosition(pos.x, pos.y); // Start position
        particleEffect.setPosition(pos.x + 10, pos.y);
        // Create the up and down movement animation
        ball.addAction(Actions.forever(
            Actions.sequence(

                Actions.moveBy(0, amount, 2, Interpolation.sineOut),
                Actions.moveBy(0, -amount, 2, Interpolation.sineIn), // Move down by 100 units in 1 second
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        SoundManager.pop.play(1f);
                        particleEffect.start();


                    }
                })
            )
        ));

        return ball;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 0f);

        stage.act(delta);
        stage.draw();
        if (!particleEffect1.isComplete()) {
            System.out.println("draw");
            particleEffect1.update(delta);
            particleEffect2.update(delta);
            stage.getBatch().begin();
            particleEffect1.draw(stage.getBatch());
            particleEffect2.draw(stage.getBatch());
            stage.getBatch().end();
        }
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }


    private Actor createUi() {
        BitmapFont font = assetManager.get(AssetDescriptors.INFO);
        font.getData().setScale(0.7f);
        // Create ImageButton for single player
        TextButton.TextButtonStyle singleplayerStyle = new TextButton.TextButtonStyle();
        singleplayerStyle.font = font; // Set your desired font here

        singleplayerStyle.up = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.SINGLEPLAYER)); // Normal state
        singleplayerStyle.down = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.SINGLEPLAYER)); // Pressed state
        singleplayerStyle.over = new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.Textures.SINGLEPLAYER)); // Hover state (optional)
// Create the Singleplayer TextButton
        TextButton singleplayer = new TextButton("Singleplayer", singleplayerStyle);

        singleplayer.pad(0, 10, 0, 10);
        singleplayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                float randomPitch = 0.8f + (float) Math.random() * 0.4f;

                SoundManager.coin.play(0.5f, randomPitch, 0f);

                stage.addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        game.setScreen(new SelectionScreen(game, team1, team2, Mode.SINGLEPLAYER));
                    }
                })));
            }
        });
        singleplayer.setTransform(true); // Enable transformations like scaling
        singleplayer.setOrigin(Align.center);

// Add hover effect using ClickListener
        singleplayer.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                singleplayer.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f)); // Scale up to 120% over 0.1 seconds
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                singleplayer.addAction(Actions.scaleTo(1f, 1f, 0.1f)); // Scale back to normal
            }
        });


        TextButton multiplayer = new TextButton("Multiplayer", singleplayerStyle);
        multiplayer.setTransform(true); // Enable transformations like scaling
        multiplayer.setOrigin(Align.center);
        multiplayer.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new SelectionScreen(game, team1, team2, Mode.LOCALMULTIPLAYER));
                float randomPitch = 0.8f + (float) Math.random() * 0.4f;

                SoundManager.coin.play(0.5f, randomPitch, 0f);
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
        multiplayer.pad(0, 10, 0, 10);
        TextButton instructions = new TextButton("Instructions", singleplayerStyle);
        instructions.setTransform(true); // Enable transformations like scaling
        instructions.setOrigin(Align.center);
        instructions.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //game.setScreen(new InstructionsScreen(game));
                float randomPitch = 0.8f + (float) Math.random() * 0.4f;

                SoundManager.coin.play(0.5f, randomPitch, 0f);
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                instructions.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f)); // Scale up to 120% over 0.1 seconds
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                instructions.addAction(Actions.scaleTo(1f, 1f, 0.1f)); // Scale up to 120% over 0.1 seconds

            }
        });
        multiplayer.pad(0, 10, 0, 10);
        Table buttonTable = new Table();
        buttonTable.setFillParent(true); // Ensure the table takes up the entire stage
        Label.LabelStyle style = new Label.LabelStyle(assetManager.get(AssetDescriptors.GAMEOVER), Color.WHITE);
        Label nameLabel = new Label("Spotter", style);
        nameLabel.addAction(Actions.forever(Actions.sequence(
            Actions.scaleTo(1.5f, 1.5f, 0.5f, Interpolation.sineIn), // Scale up to 1.5x size
            Actions.scaleTo(1f, 1f, 0.5f, Interpolation.sineOut) // Scale down back to original size
        )));


        // Add buttons to the table
        buttonTable.defaults().pad(10); // Add padding between buttons
        buttonTable.add(nameLabel).center().row();
        buttonTable.add(singleplayer).center().row();
        buttonTable.add(multiplayer).center().row();
        buttonTable.add(instructions).center().row();


        return buttonTable;

    }


}
