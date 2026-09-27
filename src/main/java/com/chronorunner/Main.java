package com.chronorunner;

import com.chronorunner.audio.SoundManager;
import com.chronorunner.data.GameData;
import com.chronorunner.player.PlayerController;
import com.chronorunner.ui.LoadingScreen;
import com.chronorunner.ui.MenuManager;
import com.chronorunner.util.SceneUtil;
import com.chronorunner.world.WorldManager;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.FileLocator;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.ui.Picture;

import java.io.File;

public class Main extends SimpleApplication implements ActionListener {

    private GameMode mode = GameMode.LOADING;

    private GameData gameData;
    private LoadingScreen loadingScreen;
    private MenuManager menuManager;
    private SoundManager soundManager;
    private PlayerController player;
    private WorldManager worldManager;

    /*
     * runCoins = coins collected only in current gameplay run.
     * gameData coins = saved total coins shown on main menu.
     */
    private int runCoins = 0;
    private boolean runCoinsSaved = false;

    /*
     * Gameplay HUD bars.
     */
    private Picture coinBarPicture;
    private Picture speedBarPicture;
    private BitmapText coinText;
    private BitmapText speedText;
    private BitmapText turnText;

    /*
     * Main menu saved coin bar.
     */
    private Picture menuCoinBarPicture;
    private BitmapText menuCoinText;

    /*
     * Game over image.
     */
    private Picture gameOverPicture;

    public static void main(String[] args) {
        Main app = new Main();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("Chrono Runner - Volcano Escape");
        settings.setResolution(GameConfig.WIDTH, GameConfig.HEIGHT);
        settings.setVSync(true);

        app.setSettings(settings);
        app.setShowSettings(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        registerAssetsFolder();

        setDisplayStatView(false);
        setDisplayFps(false);

        flyCam.setEnabled(false);
        viewPort.setBackgroundColor(new ColorRGBA(0.015f, 0.01f, 0.015f, 1f));
        inputManager.setCursorVisible(true);

        gameData = new GameData();

        SceneUtil.addBasicLights(rootNode);

        loadingScreen = new LoadingScreen(guiNode, assetManager);
        loadingScreen.show();

        menuManager = new MenuManager(guiNode, assetManager, gameData);
        soundManager = new SoundManager(assetManager, rootNode, gameData);

        setupHud();
        setupInput();
    }

    private void registerAssetsFolder() {
        File assetsFolder = new File(System.getProperty("user.dir"), "assets");

        System.out.println("Current working directory: " + System.getProperty("user.dir"));
        System.out.println("Looking for assets at: " + assetsFolder.getAbsolutePath());
        System.out.println("Assets folder exists: " + assetsFolder.exists());

        if (assetsFolder.exists()) {
            assetManager.registerLocator(assetsFolder.getAbsolutePath(), FileLocator.class);
            System.out.println("Assets folder registered successfully.");
        } else {
            System.out.println("ERROR: assets folder was not found.");
        }
    }

    private void setupHud() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        /*
         * Gameplay coin bar.
         * Smaller than before and text is placed inside the correct number area.
         */
        coinBarPicture = new Picture("Gameplay Coin Bar");
        coinBarPicture.setImage(assetManager, AssetPaths.COIN_BAR, true);
        coinBarPicture.setWidth(190f);
        coinBarPicture.setHeight(36f);
        coinBarPicture.setPosition(8f, GameConfig.HEIGHT - 45f);
        coinBarPicture.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(coinBarPicture);

        coinText = new BitmapText(font);
        coinText.setSize(18);
        coinText.setColor(new ColorRGBA(1f, 0.95f, 0.20f, 1f));
        coinText.setText("0");
        coinText.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(coinText);

        /*
         * Gameplay speed bar.
         * Same size as coin bar, placed on right side.
         */
        speedBarPicture = new Picture("Gameplay Speed Bar");
        speedBarPicture.setImage(assetManager, AssetPaths.SPEED_BAR, true);
        speedBarPicture.setWidth(190f);
        speedBarPicture.setHeight(36f);
        speedBarPicture.setPosition(GameConfig.WIDTH - 198f, GameConfig.HEIGHT - 45f);
        speedBarPicture.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(speedBarPicture);

        speedText = new BitmapText(font);
        speedText.setSize(18);
        speedText.setColor(new ColorRGBA(1f, 0.95f, 0.20f, 1f));
        speedText.setText("0.0");
        speedText.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(speedText);

        /*
         * Main menu saved coin bar.
         * Bigger and centered because only one bar is shown on menu.
         */
        menuCoinBarPicture = new Picture("Menu Coin Bar");
        menuCoinBarPicture.setImage(assetManager, AssetPaths.COIN_BAR, true);
        menuCoinBarPicture.setWidth(260f);
        menuCoinBarPicture.setHeight(48f);
        menuCoinBarPicture.setPosition((GameConfig.WIDTH - 260f) / 2f, GameConfig.HEIGHT - 62f);
        menuCoinBarPicture.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(menuCoinBarPicture);

        menuCoinText = new BitmapText(font);
        menuCoinText.setSize(22);
        menuCoinText.setColor(new ColorRGBA(1f, 0.95f, 0.20f, 1f));
        menuCoinText.setText("0");
        menuCoinText.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(menuCoinText);

        /*
         * Turn instruction.
         */
        turnText = new BitmapText(font);
        turnText.setSize(26);
        turnText.setColor(ColorRGBA.Orange);
        turnText.setText("");
        turnText.setLocalTranslation(70, GameConfig.HEIGHT - 90, 10);
        turnText.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(turnText);

        /*
         * Game over image.
         */
        gameOverPicture = new Picture("Game Over Image");
        gameOverPicture.setImage(assetManager, AssetPaths.GAME_OVER_IMAGE, true);
        gameOverPicture.setWidth(310f);
        gameOverPicture.setHeight(190f);
        gameOverPicture.setPosition((GameConfig.WIDTH - 310f) / 2f, 275f);
        gameOverPicture.setCullHint(Spatial.CullHint.Always);
        guiNode.attachChild(gameOverPicture);

        updateMenuCoinText();
    }

    private void setupInput() {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("Slide", new KeyTrigger(KeyInput.KEY_DOWN));

        inputManager.addMapping("Play", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("Settings", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Quit", new KeyTrigger(KeyInput.KEY_Q));
        inputManager.addMapping("ToggleSound", new KeyTrigger(KeyInput.KEY_M));
        inputManager.addMapping("Restart", new KeyTrigger(KeyInput.KEY_R));

        inputManager.addMapping("MouseClick", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addListener(
                this,
                "Left",
                "Right",
                "Jump",
                "Slide",
                "Play",
                "Settings",
                "Quit",
                "ToggleSound",
                "Restart",
                "MouseClick"
        );
    }

    @Override
    public void simpleUpdate(float tpf) {
        switch (mode) {
            case LOADING -> updateLoading(tpf);
            case PLAYING -> updateGame(tpf);
            default -> {
            }
        }
    }

    private void updateLoading(float tpf) {
        boolean complete = loadingScreen.update(tpf);

        if (complete) {
            loadingScreen.hide();

            mode = GameMode.MENU;
            inputManager.setCursorVisible(true);

            menuManager.show();
            showMenuCoinBar();
            soundManager.playMenuMusic();
        }
    }

    private void startGame() {
        menuManager.hide();
        hideMenuCoinBar();

        inputManager.setCursorVisible(false);

        hideGameOverImage();

        /*
         * Every new game run starts from 0 coins.
         */
        runCoins = 0;
        runCoinsSaved = false;

        showHud();
        updateHudText();

        if (player == null) {
            player = new PlayerController(assetManager, rootNode);
        } else {
            player.reset();
        }

        if (worldManager == null) {
            worldManager = new WorldManager(assetManager, rootNode);
        }

        worldManager.startWorld();
        player.setSpeed(worldManager.getCurrentSpeed());

        mode = GameMode.PLAYING;

        soundManager.playRunning();
    }

    private void updateGame(float tpf) {
        if (player == null || !player.isAlive()) {
            return;
        }

        player.setSpeed(worldManager.getCurrentSpeed());
        player.update(tpf);

        worldManager.update(
                tpf,
                player,
                () -> {
                    runCoins++;
                    soundManager.playCoin();
                },
                this::gameOver
        );

        updateCamera();
        updateHudText();
    }

    private void updateHudText() {
        setTextOnBar(
                coinText,
                String.valueOf(runCoins),
                8f,
                190f,
                GameConfig.HEIGHT - 22f,
                62f,
                22f
        );

        setTextOnBar(
                speedText,
                String.format("%.1f", worldManager == null ? 0f : worldManager.getCurrentSpeed()),
                GameConfig.WIDTH - 198f,
                190f,
                GameConfig.HEIGHT - 22f,
                62f,
                22f
        );

        if (worldManager != null && player != null && worldManager.isPlayerInTurnWindow(player)) {
            turnText.setText("TURN " + worldManager.getRequiredTurnText() + "!");
            turnText.setCullHint(Spatial.CullHint.Inherit);
        } else {
            turnText.setText("");
            turnText.setCullHint(Spatial.CullHint.Always);
        }
    }

    private void updateMenuCoinText() {
        setTextOnBar(
                menuCoinText,
                String.valueOf(gameData.getCoins()),
                (GameConfig.WIDTH - 260f) / 2f,
                260f,
                GameConfig.HEIGHT - 31f,
                86f,
                28f
        );
    }

    private void setTextOnBar(
            BitmapText text,
            String value,
            float barX,
            float barWidth,
            float textY,
            float iconAreaWidth,
            float rightPadding
    ) {
        text.setText(value);

        /*
         * Left side of bar contains icon.
         * Number should be centered in the right usable area.
         */
        float numberAreaX = barX + iconAreaWidth;
        float numberAreaWidth = barWidth - iconAreaWidth - rightPadding;

        float textWidth = text.getLineWidth();
        float x = numberAreaX + (numberAreaWidth - textWidth) / 2f;

        text.setLocalTranslation(x, textY, 10f);
    }

    private void updateCamera() {
        Vector3f p = player.getPosition();
        Vector3f forward = player.getForwardDirection();
        Vector3f right = player.getRightDirection();

        Vector3f camPos = p
                .subtract(forward.mult(8.5f))
                .add(Vector3f.UNIT_Y.mult(4.2f))
                .add(right.mult(0.2f));

        cam.setLocation(cam.getLocation().interpolateLocal(camPos, 0.18f));

        cam.lookAt(
                p.add(forward.mult(8.0f)).add(Vector3f.UNIT_Y.mult(1.5f)),
                Vector3f.UNIT_Y
        );
    }

    private void gameOver() {
        if (mode != GameMode.PLAYING) {
            return;
        }

        player.die();

        soundManager.stopRunning();
        soundManager.playDeath();

        saveRunCoinsToTotal();

        mode = GameMode.GAME_OVER;

        inputManager.setCursorVisible(true);

        hideHud();
        showGameOverImage();

        System.out.println("GAME OVER triggered");
    }

    private void saveRunCoinsToTotal() {
        if (runCoinsSaved) {
            return;
        }

        if (runCoins > 0) {
            gameData.addCoins(runCoins);
        }

        runCoinsSaved = true;
        updateMenuCoinText();
    }

    private void restartGame() {
        if (mode == GameMode.GAME_OVER) {
            hideGameOverImage();
            startGame();
        }
    }

    private void returnToMainMenu() {
        if (mode == GameMode.PLAYING) {
            saveRunCoinsToTotal();
        }

        hideHud();
        hideGameOverImage();

        soundManager.stopRunning();

        mode = GameMode.MENU;

        inputManager.setCursorVisible(true);

        menuManager.show();
        updateMenuCoinText();
        showMenuCoinBar();
        soundManager.playMenuMusic();
    }

    private void showHud() {
        coinBarPicture.setCullHint(Spatial.CullHint.Inherit);
        speedBarPicture.setCullHint(Spatial.CullHint.Inherit);
        coinText.setCullHint(Spatial.CullHint.Inherit);
        speedText.setCullHint(Spatial.CullHint.Inherit);
        turnText.setCullHint(Spatial.CullHint.Always);
    }

    private void hideHud() {
        coinBarPicture.setCullHint(Spatial.CullHint.Always);
        speedBarPicture.setCullHint(Spatial.CullHint.Always);
        coinText.setCullHint(Spatial.CullHint.Always);
        speedText.setCullHint(Spatial.CullHint.Always);
        turnText.setCullHint(Spatial.CullHint.Always);
    }

    private void showMenuCoinBar() {
        updateMenuCoinText();
        menuCoinBarPicture.setCullHint(Spatial.CullHint.Inherit);
        menuCoinText.setCullHint(Spatial.CullHint.Inherit);
    }

    private void hideMenuCoinBar() {
        menuCoinBarPicture.setCullHint(Spatial.CullHint.Always);
        menuCoinText.setCullHint(Spatial.CullHint.Always);
    }

    private void showGameOverImage() {
        gameOverPicture.setCullHint(Spatial.CullHint.Inherit);
    }

    private void hideGameOverImage() {
        gameOverPicture.setCullHint(Spatial.CullHint.Always);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (!isPressed) {
            return;
        }

        if (mode == GameMode.MENU) {
            handleMenuInput(name);
            return;
        }

        if (mode == GameMode.PLAYING) {
            handleGameInput(name);
            return;
        }

        if (mode == GameMode.GAME_OVER) {
            handleGameOverInput(name);
        }
    }

    private void handleMenuInput(String name) {
        switch (name) {
            case "Play" -> startGame();

            case "Settings" -> menuManager.openSettingsByKeyboard();

            case "ToggleSound" -> toggleSound();

            case "Restart" -> menuManager.backToMainByKeyboard();

            case "Quit" -> stop();

            case "MouseClick" -> {
                MenuManager.MenuAction action = menuManager.click(inputManager.getCursorPosition());

                if (action == MenuManager.MenuAction.PLAY) {
                    startGame();
                } else if (action == MenuManager.MenuAction.QUIT) {
                    stop();
                } else if (action == MenuManager.MenuAction.TOGGLE_SOUND) {
                    toggleSound();
                }
            }
        }
    }

    private void handleGameInput(String name) {
        if (player == null || worldManager == null) {
            return;
        }

        switch (name) {
            case "Left" -> {
                if (!handleTurnInput(WorldManager.TurnInput.LEFT)) {
                    player.moveLeft();
                }
            }

            case "Right" -> {
                if (!handleTurnInput(WorldManager.TurnInput.RIGHT)) {
                    player.moveRight();
                }
            }

            case "Jump" -> {
                player.jump();
                soundManager.playJump();
            }

            case "Slide" -> player.slide();

            case "Quit" -> returnToMainMenu();
        }
    }

    private boolean handleTurnInput(WorldManager.TurnInput input) {
        if (!worldManager.isPlayerInTurnWindow(player)) {
            return false;
        }

        boolean correct = worldManager.handleTurnInput(input, player);

        if (!correct) {
            gameOver();
        } else {
            player.setSpeed(worldManager.getCurrentSpeed());
        }

        return true;
    }

    private void handleGameOverInput(String name) {
        switch (name) {
            case "Restart" -> restartGame();

            case "Quit" -> returnToMainMenu();
        }
    }

    private void toggleSound() {
        gameData.setSoundOn(!gameData.isSoundOn());

        menuManager.refresh();
        soundManager.refreshAfterSoundToggle();
    }
}