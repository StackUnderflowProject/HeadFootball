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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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


public class SelectionScreen extends ScreenAdapter {

    private final SoccerGame game;
    private final AssetManager assetManager;

    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private Team team11;
    private Team team2;
    private PlayerInputProcessor1 ip1;
    private PlayerInputProcessor1 ip2;
    private PlayerInputProcessorCpu ipcpu;
    private Mode mode;
    private TextureAtlas gameplayAtlas;

    public SelectionScreen(SoccerGame game,Team team1,Team team2,Mode mode) {
        this.game = game;
        viewport = new StretchViewport(GameConfig.HUD_WIDTH,GameConfig.HUD_HEIGHT);
        this.team11 = team1;
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
        rootTable.setBackground(new TextureRegionDrawable(gameplayAtlas.findRegion("t")));
        stage.addActor(rootTable);
        skin.getFont("font-label").setColor(Color.BLACK);

        // Title Label
        Label title = new Label("Player Select Screen", skin);
        title.setColor(Color.BLACK);
        rootTable.add(title).colspan(2).pad(20);
        rootTable.row();


        Table playerIconsTable = new Table();
        Table player1 = createPlayerIcon("Player 1", RegionNames.Textures.P1).pad(10);
        Table player2 = createPlayerIcon("Player 2", RegionNames.Textures.P2).pad(10);

        if(mode == Mode.SINGLEPLAYER){
            player2 = createPlayerIcon("Player 2", RegionNames.Textures.CPU).pad(10);
        }
        // Create Team Boxes
        Table team1Table = createTeamBox(team11,player1);
        Table team2Table = createTeamBox(team2,player1);

        rootTable.add(team1Table).minWidth(160).minHeight(150).maxWidth(300).maxHeight(300).expand().pad(0);
        rootTable.add(team2Table).minWidth(150).minHeight(150).maxWidth(300).maxHeight(300).expand().pad(0);
        // Add Player Icons under the Team Tables
        rootTable.row(); // Move to the next row for player icons

        playerIconsTable.add(player2).pad(10);
        playerIconsTable.add(player1).pad(10);

        rootTable.add(playerIconsTable).minSize(70).colspan(2).center().padTop(10).row(); // Align icons below both team tables
        TextButton confirmButton = new TextButton("Confirm Selection", skin);
        confirmButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Handle selection confirmation
                if(mode == Mode.LOCALMULTIPLAYER){
                    if(ip1.getPlayer1Team() == ip2.getPlayer1Team()){
                        if(ip2.getPlayer1Team() != ip2.getInital()){
                            game.setScreen(new GameScreen(game,team2,team11,mode));
                            return;
                        }
                        if(ip1.getPlayer1Team() != ip1.getInital()){
                            game.setScreen(new GameScreen(game,team2,team11,mode));
                            return;

                        }
                        else{
                            game.setScreen(new GameScreen(game,team11,team2,mode));
                        }
                    }
                    else{
                        if(ip1.getPlayer1Team() == 1){
                            game.setScreen(new GameScreen(game,team11,team2,mode));
                        }
                        else{
                            game.setScreen(new GameScreen(game,team2,team11,mode));
                        }
                    }
                }
                if(mode == Mode.SINGLEPLAYER){
                    if(ipcpu.getPl() == 1){
                        game.setScreen(new GameScreen(game,team11,team2,mode));
                    }
                    else{
                        game.setScreen(new GameScreen(game,team2,team11,mode));

                    }
                }


            }
        });
        rootTable.add(confirmButton).colspan(2).center().padTop(20); // Center the button and add some padding

        if(mode == Mode.SINGLEPLAYER){
            InputMultiplexer inputMultiplexer = new InputMultiplexer();

            ipcpu = new PlayerInputProcessorCpu(player1, player2,
                Input.Keys.A, Input.Keys.D, team1Table, team2Table, playerIconsTable);
            inputMultiplexer.addProcessor(ipcpu);

            inputMultiplexer.addProcessor(stage); // Make sure you pass the Stage instance here

            Gdx.input.setInputProcessor(inputMultiplexer);


        } else if (mode == Mode.LOCALMULTIPLAYER) {
            InputMultiplexer a = setupPlayerInput(player1, player2, team1Table, team2Table, playerIconsTable);
            a.addProcessor(stage);
            Gdx.input.setInputProcessor(a);

        }

    }
    private InputMultiplexer setupPlayerInput(Table player1Icon, Table player2Icon,Table team2Table, Table team1Table, Table playerTable) {
        ip1 = new PlayerInputProcessor1(player1Icon,player2Icon,Input.Keys.RIGHT, Input.Keys.LEFT, team1Table, team2Table,playerTable,1);
        ip2 =            new PlayerInputProcessor1(player2Icon,player1Icon,Input.Keys.D, Input.Keys.A, team1Table, team2Table,playerTable,2);

        return new InputMultiplexer(
            ip1,ip2

        );
    }


    private static class PlayerInputProcessor1 implements InputProcessor {
        private final int leftKey, rightKey;
         private Table team1, team2, playerTable;
        private Table selectedTeam = null;
        private final Table player1,player2;
        private int player1Team,inital;
        public PlayerInputProcessor1(Table player,Table player2, int leftKey, int rightKey, Table team1, Table team2, Table playerTable,int pl) {
            this.player1 = player;
            this.player2 = player2;
            this.leftKey = leftKey;
            this.rightKey = rightKey;
            this.team1 = team1;
            this.team2 = team2;
            this.playerTable = playerTable;
            this.player1Team = pl;
            this.inital = pl;
        }
        public int getPlayer1Team(){
            return player1Team;
        }
        public int getInital(){
            return inital;
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
                        player1Team = inital;
                    }
                    else{
                        if(team1.findActor("placeholder") != null){
                            playerTable.removeActor(player1);
                            Cell cell = team1.getCells().get(team1.getCells().size-1);
                            cell.setActor(player1);
                            selectedTeam = team1;
                            player1Team = 2;
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
                        player1Team = inital;
                    }
                    else{
                        if(team2.findActor("placeholder") != null){
                            playerTable.removeActor(player1);
                            Cell cell = team2.getCells().get(team2.getCells().size-1);
                            cell.setActor(player1).expand().fill();
                            selectedTeam = team2;
                            player1Team = 1;
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
        private Table team1, team2;
        private final Table player1,player2;
        private int pl = 1;

        public PlayerInputProcessorCpu(Table player,Table player2, int leftKey, int rightKey, Table team1, Table team2, Table playerTable) {
            this.player1 = player;
            this.player2 = player2;
            this.leftKey = leftKey;
            this.rightKey = rightKey;
            this.team1 = team1;
            this.team2 = team2;
        }
        public int getPl(){
            return pl;
        }
        @Override
        public boolean keyDown(int keycode) {
            if (keycode == leftKey) {
                // Move player to Team 2
                Cell cell = team1.getCells().get(team1.getCells().size - 1);
                cell.setActor(player2);
                Cell cell2 = team2.getCells().get(team2.getCells().size - 1);
                cell2.setActor(player1);
                pl = 2;
            }

            if (keycode == rightKey) {
                // Move player to Team 2
                Cell cell = team1.getCells().get(team1.getCells().size - 1);

                cell.setActor(player1);
                Cell cell2 = team2.getCells().get(team2.getCells().size - 1);
                cell2.setActor(player2);
                pl = 1;
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
        Pixmap pixmap1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap1.setColor(Color.valueOf("3E5879" )); // Set color #3A3960
        pixmap1.fill();

// Create a Texture from the Pixmap
        Texture texture = new Texture(pixmap1);

// Create a Drawable from the Texture
        Drawable colorBackground = new TextureRegionDrawable(texture);
        Table teamTable = new Table();
    teamTable.setBackground(colorBackground);
        // Create a sub-table for the team name with a black background.
        Table nameTable = new Table();
        //teamTable.setBackground(new TextureRegonDrawable());
        Label teamLabel = new Label(team.getName(), skin);
        //teamLabel.setColor(Color.BLACK);

        nameTable.add(teamLabel).center().expandX().pad(0);

        // Create a sub-table for the team photo background.
        Table photoTable = new Table();
        photoTable.setBackground(new TextureRegionDrawable(team.getTextureRegion())); // Team photo as background.

        Actor placeholder1 = new Actor();
        placeholder1.setName("placeholder");
        placeholder1.setSize(player1.getWidth(), player1.getHeight());

        teamTable.add(nameTable).padBottom(5); // Team name section with fixed height.
        teamTable.row();
        teamTable.add(photoTable).growX().maxWidth(130).maxHeight(130).expandY().fillY().row(); // Team photo section fills the remaining space.

        teamTable.add(placeholder1).minSize(70).expand().fillX().row();
        return teamTable;
    }




    private Table createPlayerIcon(String playerName, String textureRegion) {
        Table playerTable = new Table();


        // Player Icon
        Image playerIcon = new Image(gameplayAtlas.findRegion(textureRegion));
        playerTable.add(playerIcon).size(60, 60).row(); // Adjust size as needed

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
