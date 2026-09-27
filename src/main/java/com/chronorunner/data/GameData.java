package com.chronorunner.data;

import java.util.prefs.Preferences;

public class GameData {
    private static final String KEY_TOTAL_COINS = "totalCoins";
    private static final String KEY_SOUND_ON = "soundOn";

    private final Preferences prefs;

    private int coins;
    private boolean soundOn;

    public GameData() {
        prefs = Preferences.userNodeForPackage(GameData.class);

        coins = prefs.getInt(KEY_TOTAL_COINS, 0);
        soundOn = prefs.getBoolean(KEY_SOUND_ON, true);
    }

    public int getCoins() {
        return coins;
    }

    public void addCoin() {
        coins++;
        saveCoins();
    }

    public void addCoins(int amount) {
        if (amount <= 0) return;

        coins += amount;
        saveCoins();
    }

    public void resetCoins() {
        coins = 0;
        saveCoins();
    }

    public boolean isSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
        prefs.putBoolean(KEY_SOUND_ON, soundOn);
    }

    private void saveCoins() {
        prefs.putInt(KEY_TOTAL_COINS, coins);
    }
}