package com.chronorunner.ui;

import com.chronorunner.AssetPaths;
import com.chronorunner.GameConfig;
import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.jme3.ui.Picture;

public class LoadingScreen {
    private final Node guiNode;
    private final AssetManager assetManager;
    private final Node loadingNode = new Node("LoadingScreen");

    private Picture fullBar;
    private float progress = 0f;

    public LoadingScreen(Node guiNode, AssetManager assetManager) {
        this.guiNode = guiNode;
        this.assetManager = assetManager;
        build();
    }

    private void build() {
        Picture bg = new Picture("Loading Background");
        bg.setImage(assetManager, AssetPaths.LOADING_BACKGROUND, true);
        bg.setWidth(GameConfig.WIDTH);
        bg.setHeight(GameConfig.HEIGHT);
        bg.setPosition(0, 0);
        loadingNode.attachChild(bg);

        float barWidth = 260f;
        float barHeight = 32f;
        float barX = (GameConfig.WIDTH - barWidth) / 2f;
        float barY = 95f;

        Picture emptyBar = new Picture("Empty Loading Bar");
        emptyBar.setImage(assetManager, AssetPaths.EMPTY_LOADING_BAR, true);
        emptyBar.setWidth(barWidth);
        emptyBar.setHeight(barHeight);
        emptyBar.setPosition(barX, barY);
        loadingNode.attachChild(emptyBar);

        fullBar = new Picture("Full Loading Bar");
        fullBar.setImage(assetManager, AssetPaths.FULL_LOADING_BAR, true);
        fullBar.setWidth(barWidth);
        fullBar.setHeight(barHeight);
        fullBar.setPosition(barX, barY);
        fullBar.setLocalScale(0.01f, 1f, 1f);
        loadingNode.attachChild(fullBar);
    }

    public void show() {
        guiNode.attachChild(loadingNode);
    }

    public void hide() {
        loadingNode.removeFromParent();
    }

    public boolean update(float tpf) {
        progress += tpf * 0.45f;

        if (progress > 1f) {
            progress = 1f;
        }

        fullBar.setLocalScale(progress, 1f, 1f);

        return progress >= 1f;
    }
}