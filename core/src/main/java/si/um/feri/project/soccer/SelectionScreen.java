package si.um.feri.project.soccer;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;


public class SelectionScreen extends ScreenAdapter {

    private final SoccerGame game;
    private final AssetManager assetManager;

    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Team team1;
    private Team team2;
    private Mode mode;
    private TextureAtlas gameplayAtlas;

    public SelectionScreen(SoccerGame game,Team team1,Team team2,Mode mode) {
        this.game = game;
        viewport = new StretchViewport(GameConfig.HUD_WIDTH,GameConfig.HUD_HEIGHT);
        this.team1 = team1;
        this.team2 = team2;
        this.mode = mode;
        stage = new Stage(viewport, game.getBatch());

        assetManager = game.getAssetManager();

        TextureAtlas atlas = assetManager.get(AssetDescriptors.GAMEPLAY);
    }

    @Override
    public void show() {

        skin = assetManager.get(AssetDescriptors.UI_SKIN);
        gameplayAtlas = assetManager.get(AssetDescriptors.GAMEPLAY);
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);
        rootTable.debug();
        // Title Label
        Label title = new Label("Player Select Screen", skin);
        rootTable.add(title).colspan(2).pad(20);
        rootTable.row();


        Table playerIconsTable = new Table();
        Table player1 = createPlayerIcon("Player 1", RegionNames.Textures.P1).pad(10);
        Table player2 = createPlayerIcon("Player 2", RegionNames.Textures.P2).pad(10);

        if(mode == Mode.SINGLEPLAYER){
            player2 = createPlayerIcon("Player 2", RegionNames.Textures.CPU).pad(10);
        }
        // Create Team Boxes
        Table team1Table = createTeamBox(team1,player1);
        Table team2Table = createTeamBox(team2,player1);

        rootTable.add(team1Table).minWidth(150).minHeight(150).maxWidth(300).maxHeight(300).expand().pad(20);
        rootTable.add(team2Table).minWidth(150).minHeight(150).maxWidth(300).maxHeight(300).expand().pad(20);
        // Add Player Icons under the Team Tables
        rootTable.row(); // Move to the next row for player icons

        playerIconsTable.add(player2).pad(10);
        playerIconsTable.add(player1).pad(10);

        rootTable.add(playerIconsTable).minSize(70).colspan(2).center().padTop(10); // Align icons below both team tables
        // Add DragAndDrop functionality
        //setupDragAndDrop(team1Table, team2Table);
        //stage.addActor(createUi())
        // Gdx.input.setInputProcessor(stage);
        if(mode == Mode.SINGLEPLAYER){
            Gdx.input.setInputProcessor(new PlayerInputProcessorCpu(player1,player2,Input.Keys.RIGHT,Input.Keys.LEFT,team1Table,team2Table,playerIconsTable));

        } else if (mode == Mode.LOCALMULTIPLAYER) {
            setupPlayerInput(player1,player2,team1Table,team2Table,playerIconsTable);

        }

    }
    private void setupPlayerInput(Table player1Icon, Table player2Icon,Table team2Table, Table team1Table, Table playerTable) {
        // Create player states

        Gdx.input.setInputProcessor(new InputMultiplexer(
            new PlayerInputProcessor1(player1Icon,player2Icon,Input.Keys.RIGHT, Input.Keys.LEFT, team1Table, team2Table,playerTable),
           new PlayerInputProcessor1(player2Icon,player1Icon,Input.Keys.D, Input.Keys.A, team1Table, team2Table,playerTable)
        ));
    }


    private static class PlayerInputProcessor1 implements InputProcessor {
        private final int leftKey, rightKey;
        private Table team1, team2, playerTable;
        private Table selectedTeam = null;
        private final Table player1,player2;

        public PlayerInputProcessor1(Table player,Table player2, int leftKey, int rightKey, Table team1, Table team2, Table playerTable) {
            this.player1 = player;
            this.player2 = player2;
            this.leftKey = leftKey;
            this.rightKey = rightKey;
            this.team1 = team1;
            this.team2 = team2;
            this.playerTable = playerTable;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == leftKey) {
                // Move player to Team 2
                if (selectedTeam != team1) {
                    if (selectedTeam != null) {
                        Cell cell = selectedTeam.getCells().get(selectedTeam.getCells().size - 1);

                        // Replace the player actor in the cell with a placeholder actor.
                        Actor placeholder = new Actor();
                        placeholder.setName("placeholder");

                        // Set the placeholder to have the same size as the player.
                        placeholder.setSize(player1.getWidth(), player1.getHeight());

                        // Update the cell with the placeholder and match its size to the player's size.
                        cell.setActor(placeholder).size(player1.getWidth(), player1.getHeight()).expand().fill();

                        // Return the player to the player table.
                        playerTable.add(player1).expand().fill();

                        // Reset the selected team reference.
                        selectedTeam = null;
                    }
                    else{
                        if(team1.findActor("placeholder") != null){
                            playerTable.removeActor(player1);
                            Cell cell = team1.getCells().get(team1.getCells().size-1);
                            cell.setActor(player1);
                            selectedTeam = team1;
                        }

                    }

                }
                return true;
            }

            if (keycode == rightKey) {
                // Move player to Team 2
                if (selectedTeam != team2) {
                    if (selectedTeam != null) {
                        Cell cell = selectedTeam.getCells().get(selectedTeam.getCells().size - 1);

                        // Replace the player actor in the cell with a placeholder actor.
                        Actor placeholder = new Actor();
                        placeholder.setName("placeholder");

                        // Set the placeholder to have the same size as the player.
                        placeholder.setSize(player1.getWidth(), player1.getHeight());

                        // Update the cell with the placeholder and match its size to the player's size.
                        cell.setActor(placeholder).size(player1.getWidth(), player1.getHeight()).expand().fill();

                        // Return the player to the player table.
                        playerTable.add(player1).expand().fill();

                        // Reset the selected team reference.
                        selectedTeam = null;
                    }
                    else{
                        if(team2.findActor("placeholder") != null){
                            playerTable.removeActor(player1);
                            Cell cell = team2.getCells().get(team2.getCells().size-1);
                            cell.setActor(player1).expand().fill();
                            selectedTeam = team2;
                        }
                    }

                }
                return true;
            }
            return false;
        }

        // Other InputProcessor methods (not used in this case)
        @Override
        public boolean keyUp(int keycode) { return false; }
        @Override
        public boolean keyTyped(char character) { return false; }
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
        @Override
        public boolean mouseMoved(int screenX, int screenY) { return false; }
        @Override
        public boolean scrolled(float amountX, float amountY) { return false; }
    }
    private static class PlayerInputProcessorCpu implements InputProcessor {
        private final int leftKey, rightKey;
        private Table team1, team2, playerTable;
        private Table selectedTeam = null;
        private final Table player1,player2;

        public PlayerInputProcessorCpu(Table player,Table player2, int leftKey, int rightKey, Table team1, Table team2, Table playerTable) {
            this.player1 = player;
            this.player2 = player2;
            this.leftKey = leftKey;
            this.rightKey = rightKey;
            this.team1 = team1;
            this.team2 = team2;
            this.playerTable = playerTable;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == leftKey) {
                // Move player to Team 2
                Cell cell = team1.getCells().get(team1.getCells().size - 1);
                cell.setActor(player2);
                Cell cell2 = team2.getCells().get(team2.getCells().size - 1);
                cell2.setActor(player1);
            }

            if (keycode == rightKey) {
                // Move player to Team 2
                Cell cell = team1.getCells().get(team1.getCells().size - 1);
                cell.setActor(player1);
                Cell cell2 = team2.getCells().get(team2.getCells().size - 1);
                cell2.setActor(player2);
            }
            return false;
        }

        // Other InputProcessor methods (not used in this case)
        @Override
        public boolean keyUp(int keycode) { return false; }
        @Override
        public boolean keyTyped(char character) { return false; }
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
        @Override
        public boolean mouseMoved(int screenX, int screenY) { return false; }
        @Override
        public boolean scrolled(float amountX, float amountY) { return false; }
    }

    private Table createTeamBox(Team team,Table player1) {
        Table teamTable = new Table();
        teamTable.debug(); // Enables debugging to visualize the layout.

        // Create a sub-table for the team name with a black background.
        Table nameTable = new Table();
        nameTable.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(createBlackPixmap()))));
        Label teamLabel = new Label(team.getName(), skin);
        nameTable.add(teamLabel).center().expand().pad(5);

        // Create a sub-table for the team photo background.
        Table photoTable = new Table();
        photoTable.setBackground(new TextureRegionDrawable(team.getTextureRegion())); // Team photo as background.

       /* Actor placeholder = new Actor();

        placeholder.setName("placeholder1");

        photoTable.add(placeholder).expand().fill().row();*/

        Actor placeholder1 = new Actor();
        placeholder1.setName("placeholder");
        placeholder1.setSize(player1.getWidth(), player1.getHeight());

        //photoTable.add(placeholder1).expand().fill().row();
        // Add the sub-tables to the main teamTable.
        teamTable.add(nameTable).fillX().height(50).padBottom(5); // Team name section with fixed height.
        teamTable.row();
        teamTable.add(photoTable).growX().expandY().fillY().row(); // Team photo section fills the remaining space.
        //teamTable.add(placeholder1).size(player1.getWidth(), player1.getHeight()).expand().fill().row();

        teamTable.add(placeholder1).minSize(70).expand().fillX().row();
        return teamTable;
    }
    private Pixmap createBlackPixmap() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 1); // Black color with full opacity.
        pixmap.fill();
        return pixmap;
    }



    private Table createPlayerIcon(String playerName, String textureRegion) {
        Table playerTable = new Table();


        // Player Icon
        Image playerIcon = new Image(gameplayAtlas.findRegion(textureRegion));
        playerTable.add(playerIcon).size(60, 60).row(); // Adjust size as needed

        // Player Name Label
        /*Label playerLabel = new Label(playerName, skin);
        playerLabel.setAlignment(Align.center);
        playerTable.add(playerLabel).padTop(5);*/

        return playerTable;
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



    private Actor createUi() {
        BitmapFont font = assetManager.get(AssetDescriptors.TITLE_FONT);


        stage.addActor(new Image(gameplayAtlas.findRegion(RegionNames.Textures.FIELD)));
        // Create the Window
        Window window = new Window("", skin);


        // Set the size of the window dynamically
        window.setSize(viewport.getWorldWidth() * 0.5f, viewport.getWorldHeight() * 0.5f);  // Adjust window size based on viewport width
        window.setMovable(false);  // Make window non-movable


        TextButton playButton = new TextButton("Play", skin);
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
               // game.setScreen(new GameScreen(game));
            }
        });



        TextButton settingsButton = new TextButton("Settings", skin);
        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //game.setScreen(new SettingsScreen(game));
            }
        });

        TextButton quitButton = new TextButton("Quit", skin);
        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        Table buttonTable = new Table();

        buttonTable.defaults().padLeft(20).padTop(5).padRight(20).padBottom(5);

        buttonTable.add(playButton).colspan(2).expandX().fillX().row();
        buttonTable.add(settingsButton).colspan(2).fillX().row();

        buttonTable.add(quitButton).colspan(2).right().fillX().row();

        buttonTable.center();

        window.add(buttonTable).fill().expand();


        window.setPosition(stage.getWidth() / 2 - window.getWidth() / 2, stage.getHeight() / 2 - window.getHeight() / 2);

        return window;
    }




}