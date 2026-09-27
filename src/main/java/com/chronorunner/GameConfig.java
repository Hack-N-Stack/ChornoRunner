package com.chronorunner;

public final class GameConfig {
    private GameConfig() {}

    public static final int WIDTH = 420;
    public static final int HEIGHT = 740;

    public static final float START_SPEED = 15.5f;
    public static final float PLAYER_SPEED = START_SPEED;
    public static final float SPEED_INCREASE_AFTER_TURN = 2.0f;
    public static final float MAX_PLAYER_SPEED = 25f;

    public static final float LANE_WIDTH = 3.2f;

    public static final float JUMP_SPEED = 14f;
    public static final float GRAVITY = 32f;

    public static final float SEGMENT_LENGTH = 36f;
    public static final int START_SEGMENTS = 10;

    public static final float COIN_PICK_DISTANCE = 1.55f;

    // Change this to 25f if you want first turn after 25 seconds.
    public static final float FIRST_TURN_TIME = 10f;
    public static final float NEXT_TURN_INTERVAL = 35f;

    // Bigger value = player can press turn key from farther away.
    public static final float TURN_INPUT_RANGE = 6.0f;

    // If player passes the corner without correct turn, game over.
    public static final float TURN_MISS_DISTANCE = 1.4f;
}