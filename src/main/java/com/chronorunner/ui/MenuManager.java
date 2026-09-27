package com.chronorunner.ui;

import com.chronorunner.AssetPaths;
import com.chronorunner.GameConfig;
import com.chronorunner.data.GameData;
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector2f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.ui.Picture;

public class MenuManager {

    public enum MenuAction {
        NONE,
        PLAY,
        QUIT,
        TOGGLE_SOUND
    }

    private enum Screen {
        MAIN,
        SETTINGS
    }

    private final Node guiNode;
    private final AssetManager assetManager;
    private final GameData gameData;

    private final Node menuNode = new Node("Menu");
    private final Node mainNode = new Node("Main Menu");
    private final Node settingsNode = new Node("Settings Menu");

    private Picture background;

    private Picture playButton;
    private Picture settingsButton;
    private Picture quitButton;

    private Picture volumeOnButton;
    private Picture volumeOffButton;

    private Picture backButton;

    private Screen screen = Screen.MAIN;

    private float playX;
    private float playY;
    private float playW;
    private float playH;

    private float settingsX;
    private float settingsY;
    private float settingsW;
    private float settingsH;

    private float quitX;
    private float quitY;
    private float quitW;
    private float quitH;

    private float volumeX;
    private float volumeY;
    private float volumeW;
    private float volumeH;

    private float backX;
    private float backY;
    private float backW;
    private float backH;

    public MenuManager(Node guiNode, AssetManager assetManager, GameData gameData) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        this.gameData = gameData;

        build();
    }

    private void build() {
        buildBackground();
        buildMainMenu();
        buildSettingsMenu();

        menuNode.attachChild(background);
        menuNode.attachChild(mainNode);
        menuNode.attachChild(settingsNode);

        settingsNode.setCullHint(Spatial.CullHint.Always);
        menuNode.setCullHint(Spatial.CullHint.Always);

        guiNode.attachChild(menuNode);
    }

    private void buildBackground() {
        background = new Picture("Menu Background");
        background.setImage(assetManager, AssetPaths.LOADING_BACKGROUND, true);
        background.setWidth(GameConfig.WIDTH);
        background.setHeight(GameConfig.HEIGHT);
        background.setPosition(0, 0);
    }

    private void buildMainMenu() {
        float buttonW = 210f;
        float buttonH = 58f;
        float centerX = (GameConfig.WIDTH - buttonW) / 2f;

        playW = buttonW;
        playH = buttonH;
        playX = centerX;
        playY = 390f;

        playButton = new Picture("Play Button");
        playButton.setImage(assetManager, AssetPaths.PLAY_BUTTON, true);
        playButton.setWidth(playW);
        playButton.setHeight(playH);
        playButton.setPosition(playX, playY);
        mainNode.attachChild(playButton);

        settingsW = buttonW;
        settingsH = buttonH;
        settingsX = centerX;
        settingsY = 290f;

        settingsButton = new Picture("Settings Button");
        settingsButton.setImage(assetManager, AssetPaths.SETTINGS_BUTTON, true);
        settingsButton.setWidth(settingsW);
        settingsButton.setHeight(settingsH);
        settingsButton.setPosition(settingsX, settingsY);
        mainNode.attachChild(settingsButton);

        quitW = buttonW;
        quitH = buttonH;
        quitX = centerX;
        quitY = 190f;

        quitButton = new Picture("Quit Button");
        quitButton.setImage(assetManager, AssetPaths.QUIT_BUTTON, true);
        quitButton.setWidth(quitW);
        quitButton.setHeight(quitH);
        quitButton.setPosition(quitX, quitY);
        mainNode.attachChild(quitButton);
    }

    private void buildSettingsMenu() {
        float buttonW = 230f;
        float buttonH = 62f;
        float centerX = (GameConfig.WIDTH - buttonW) / 2f;

        volumeW = buttonW;
        volumeH = buttonH;
        volumeX = centerX;
        volumeY = 365f;

        volumeOnButton = new Picture("Volume On Button");
        volumeOnButton.setImage(assetManager, AssetPaths.VOLUME_ON_BUTTON, true);
        volumeOnButton.setWidth(volumeW);
        volumeOnButton.setHeight(volumeH);
        volumeOnButton.setPosition(volumeX, volumeY);
        settingsNode.attachChild(volumeOnButton);

        volumeOffButton = new Picture("Volume Off Button");
        volumeOffButton.setImage(assetManager, AssetPaths.VOLUME_OFF_BUTTON, true);
        volumeOffButton.setWidth(volumeW);
        volumeOffButton.setHeight(volumeH);
        volumeOffButton.setPosition(volumeX, volumeY);
        settingsNode.attachChild(volumeOffButton);

        backW = 170f;
        backH = 48f;
        backX = 18f;
        backY = GameConfig.HEIGHT - 118f;

        backButton = new Picture("Back Button");
        backButton.setImage(assetManager, AssetPaths.QUIT_BUTTON, true);
        backButton.setWidth(backW);
        backButton.setHeight(backH);
        backButton.setPosition(backX, backY);
        settingsNode.attachChild(backButton);

        refresh();
    }

    public void show() {
        menuNode.setCullHint(Spatial.CullHint.Inherit);
        showMainScreen();
    }

    public void hide() {
        menuNode.setCullHint(Spatial.CullHint.Always);
    }

    public void refresh() {
        if (gameData.isSoundOn()) {
            volumeOnButton.setCullHint(Spatial.CullHint.Inherit);
            volumeOffButton.setCullHint(Spatial.CullHint.Always);
        } else {
            volumeOnButton.setCullHint(Spatial.CullHint.Always);
            volumeOffButton.setCullHint(Spatial.CullHint.Inherit);
        }
    }

    public void openSettingsByKeyboard() {
        showSettingsScreen();
    }

    public void backToMainByKeyboard() {
        showMainScreen();
    }

    private void showMainScreen() {
        screen = Screen.MAIN;

        mainNode.setCullHint(Spatial.CullHint.Inherit);
        settingsNode.setCullHint(Spatial.CullHint.Always);
    }

    private void showSettingsScreen() {
        screen = Screen.SETTINGS;

        mainNode.setCullHint(Spatial.CullHint.Always);
        settingsNode.setCullHint(Spatial.CullHint.Inherit);

        refresh();
    }

    public MenuAction click(Vector2f cursorPosition) {
        float x = cursorPosition.x;
        float y = cursorPosition.y;

        if (screen == Screen.MAIN) {
            if (inside(x, y, playX, playY, playW, playH)) {
                return MenuAction.PLAY;
            }

            if (inside(x, y, settingsX, settingsY, settingsW, settingsH)) {
                showSettingsScreen();
                return MenuAction.NONE;
            }

            if (inside(x, y, quitX, quitY, quitW, quitH)) {
                return MenuAction.QUIT;
            }

            return MenuAction.NONE;
        }

        if (screen == Screen.SETTINGS) {
            if (inside(x, y, volumeX, volumeY, volumeW, volumeH)) {
                return MenuAction.TOGGLE_SOUND;
            }

            if (inside(x, y, backX, backY, backW, backH)) {
                showMainScreen();
                return MenuAction.NONE;
            }
        }

        return MenuAction.NONE;
    }

    private boolean inside(float mouseX, float mouseY, float x, float y, float w, float h) {
        return mouseX >= x
                && mouseX <= x + w
                && mouseY >= y
                && mouseY <= y + h;
    }
}