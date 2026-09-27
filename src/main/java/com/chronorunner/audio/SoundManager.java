package com.chronorunner.audio;

import com.chronorunner.AssetPaths;
import com.chronorunner.data.GameData;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import com.jme3.audio.AudioSource;
import com.jme3.scene.Node;

public class SoundManager {
    private final AssetManager assetManager;
    private final Node rootNode;
    private final GameData data;

    private AudioNode gameplayMusic;
    private AudioNode coinSound;
    private AudioNode jumpSound;
    private AudioNode deathSound;
    private AudioNode runningSound;

    public SoundManager(AssetManager assetManager, Node rootNode, GameData data) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
        this.data = data;
        loadSounds();
    }

    private AudioNode load(String path, boolean looping, boolean stream, float volume) {
        try {
            AudioNode node = new AudioNode(
                    assetManager,
                    path,
                    stream ? AudioData.DataType.Stream : AudioData.DataType.Buffer
            );

            node.setLooping(looping);
            node.setPositional(false);
            node.setVolume(volume);

            rootNode.attachChild(node);

            System.out.println("Loaded sound successfully: " + path);
            return node;

        } catch (Exception ex) {
            System.out.println("Could not load sound: " + path);
            System.out.println("Reason: " + ex.getMessage());
            return null;
        }
    }

    private void loadSounds() {
        gameplayMusic = load(AssetPaths.SOUND_GAMEPLAY, true, true, 0.45f);
        runningSound = load(AssetPaths.SOUND_RUNNING, true, true, 0.35f);

        coinSound = load(AssetPaths.SOUND_COIN, false, false, 1.0f);
        jumpSound = load(AssetPaths.SOUND_JUMP, false, false, 0.9f);
        deathSound = load(AssetPaths.SOUND_DEATH, false, false, 1.0f);
    }

    public void playMenuMusic() {
        if (!data.isSoundOn()) return;

        if (gameplayMusic != null &&
                gameplayMusic.getStatus() != AudioSource.Status.Playing) {
            gameplayMusic.play();
        }
    }

    public void playRunning() {
        if (!data.isSoundOn()) return;

        if (runningSound != null &&
                runningSound.getStatus() != AudioSource.Status.Playing) {
            runningSound.play();
        }
    }

    public void stopRunning() {
        if (runningSound != null) {
            runningSound.stop();
        }
    }

    public void playCoin() {
        if (data.isSoundOn() && coinSound != null) {
            coinSound.playInstance();
        }
    }

    public void playJump() {
        if (data.isSoundOn() && jumpSound != null) {
            jumpSound.playInstance();
        }
    }

    public void playDeath() {
        if (data.isSoundOn() && deathSound != null) {
            deathSound.playInstance();
        }
    }

    public void refreshAfterSoundToggle() {
        if (!data.isSoundOn()) {
            if (gameplayMusic != null) {
                gameplayMusic.pause();
            }

            if (runningSound != null) {
                runningSound.pause();
            }
        } else {
            playMenuMusic();
        }
    }
}