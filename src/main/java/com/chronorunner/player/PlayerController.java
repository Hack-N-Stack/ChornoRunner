package com.chronorunner.player;

import com.chronorunner.AssetPaths;
import com.chronorunner.GameConfig;
import com.jme3.anim.AnimComposer;
import com.jme3.asset.AssetManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class PlayerController {
    private final AssetManager assetManager;
    private final Node rootNode;
    private final Node playerNode = new Node("Player");

    private Spatial runModel;
    private Spatial jumpModel;
    private Spatial slideModel;
    private Spatial deathModel;
    private Spatial currentModel;

    private int lane = 0;
    private float laneOffset = 0f;

    private float yPosition = 1.2f;
    private float yVelocity = 0f;
    private boolean grounded = true;
    private float slideTimer = 0f;
    private boolean alive = true;

    private float currentSpeed = GameConfig.START_SPEED;

    private Vector3f centerPosition = new Vector3f(0f, 0f, 8f);
    private Vector3f forwardDir = new Vector3f(0f, 0f, -1f);
    private Vector3f rightDir = new Vector3f(1f, 0f, 0f);

    private static final float MODEL_SCALE = 1.0f;
    private static final float MODEL_Y_OFFSET = 0f;

    public PlayerController(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;

        loadAllModels();

        applyPlayerTransform();
        rootNode.attachChild(playerNode);

        showRun();
    }

    private void loadAllModels() {
        runModel = loadModel(AssetPaths.ANIM_RUN, "RunModel");
        jumpModel = loadModel(AssetPaths.ANIM_JUMP, "JumpModel");
        slideModel = loadModel(AssetPaths.ANIM_SLIDE, "SlideModel");
        deathModel = loadModel(AssetPaths.ANIM_DEATH, "DeathModel");

        if (runModel == null) {
            System.out.println("ERROR: run.glb could not load. Character cannot appear.");
            return;
        }

        if (jumpModel == null) {
            System.out.println("WARNING: jump.glb missing. Using run model for jump.");
            jumpModel = runModel.clone();
        }

        if (slideModel == null) {
            System.out.println("WARNING: slide.glb missing. Using run model for slide.");
            slideModel = runModel.clone();
        }

        if (deathModel == null) {
            System.out.println("WARNING: death.glb missing. Using run model for death.");
            deathModel = runModel.clone();
        }

        prepareModel(runModel);
        prepareModel(jumpModel);
        prepareModel(slideModel);
        prepareModel(deathModel);

        playerNode.attachChild(runModel);
        playerNode.attachChild(jumpModel);
        playerNode.attachChild(slideModel);
        playerNode.attachChild(deathModel);

        hideAllModels();
    }

    private Spatial loadModel(String path, String name) {
        try {
            Spatial model = assetManager.loadModel(path);
            model.setName(name);
            System.out.println("Loaded animation model: " + path);
            return model;
        } catch (Exception ex) {
            System.out.println("FAILED to load animation model: " + path);
            System.out.println("Reason: " + ex.getMessage());
            return null;
        }
    }

    private void prepareModel(Spatial model) {
        if (model == null) return;

        model.setLocalScale(MODEL_SCALE);
        model.setLocalTranslation(0, MODEL_Y_OFFSET, 0);

        /*
         * Correct for your character.
         * Do NOT change this to 0 because it makes the character face the camera.
         */
        model.rotate(0, FastMath.PI, 0);

        playFirstAnimation(model);
    }

    private void playFirstAnimation(Spatial model) {
        AnimComposer composer = findComposer(model);

        if (composer == null) {
            System.out.println("No AnimComposer found in: " + model.getName());
            return;
        }

        for (String clipName : composer.getAnimClipsNames()) {
            composer.setCurrentAction(clipName);
            System.out.println("Playing animation: " + clipName + " on " + model.getName());
            return;
        }

        System.out.println("No animation clips found in: " + model.getName());
    }

    private AnimComposer findComposer(Spatial spatial) {
        if (spatial == null) return null;

        AnimComposer composer = spatial.getControl(AnimComposer.class);

        if (composer != null) {
            return composer;
        }

        if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                AnimComposer found = findComposer(child);
                if (found != null) return found;
            }
        }

        return null;
    }

    private void hideAllModels() {
        if (runModel != null) runModel.setCullHint(Spatial.CullHint.Always);
        if (jumpModel != null) jumpModel.setCullHint(Spatial.CullHint.Always);
        if (slideModel != null) slideModel.setCullHint(Spatial.CullHint.Always);
        if (deathModel != null) deathModel.setCullHint(Spatial.CullHint.Always);
    }

    private void showModel(Spatial model) {
        if (model == null) return;

        hideAllModels();

        currentModel = model;
        currentModel.setCullHint(Spatial.CullHint.Inherit);

        playFirstAnimation(currentModel);
    }

    private void showRun() {
        showModel(runModel);
    }

    private void showJump() {
        showModel(jumpModel);
    }

    private void showSlide() {
        showModel(slideModel);
    }

    private void showDeath() {
        showModel(deathModel);
    }

    public void update(float tpf) {
        if (!alive) return;

        centerPosition.addLocal(
                forwardDir.x * currentSpeed * tpf,
                0f,
                forwardDir.z * currentSpeed * tpf
        );

        float targetLaneOffset = lane * GameConfig.LANE_WIDTH;

        laneOffset = FastMath.interpolateLinear(
                Math.min(1f, tpf * 10f),
                laneOffset,
                targetLaneOffset
        );

        if (!grounded) {
            yVelocity -= GameConfig.GRAVITY * tpf;
            yPosition += yVelocity * tpf;

            if (yPosition <= 1.2f) {
                yPosition = 1.2f;
                yVelocity = 0f;
                grounded = true;
                showRun();
            }
        }

        if (slideTimer > 0f) {
            slideTimer -= tpf;

            if (slideTimer <= 0f) {
                slideTimer = 0f;
                showRun();
            }
        }

        applyPlayerTransform();
    }

    private void applyPlayerTransform() {
        Vector3f finalPos = centerPosition.add(rightDir.mult(laneOffset));
        finalPos.y = yPosition;

        playerNode.setLocalTranslation(finalPos);

        /*
         * IMPORTANT FIX:
         * Previous formula was:
         * float yaw = FastMath.atan2(forwardDir.x, -forwardDir.z);
         *
         * That rotates the character wrong after left/right turns.
         * This corrected formula makes the character face the actual path direction.
         */
        float yaw = FastMath.atan2(-forwardDir.x, -forwardDir.z);
        playerNode.setLocalRotation(new Quaternion().fromAngleAxis(yaw, Vector3f.UNIT_Y));
    }

    public void moveLeft() {
        if (!alive) return;

        lane = Math.max(-1, lane - 1);

        if (grounded && slideTimer <= 0f) {
            showRun();
        }
    }

    public void moveRight() {
        if (!alive) return;

        lane = Math.min(1, lane + 1);

        if (grounded && slideTimer <= 0f) {
            showRun();
        }
    }

    public void jump() {
        if (!alive) return;

        if (grounded && slideTimer <= 0f) {
            grounded = false;
            yVelocity = GameConfig.JUMP_SPEED;
            showJump();
        }
    }

    public void slide() {
        if (!alive) return;

        if (grounded) {
            slideTimer = 0.8f;
            showSlide();
        }
    }

    public void die() {
        if (!alive) return;

        alive = false;
        showDeath();
    }

    public void reset() {
        alive = true;

        lane = 0;
        laneOffset = 0f;

        grounded = true;
        slideTimer = 0f;
        yVelocity = 0f;
        yPosition = 1.2f;

        currentSpeed = GameConfig.START_SPEED;

        centerPosition.set(0f, 0f, 8f);
        forwardDir.set(0f, 0f, -1f);
        rightDir.set(1f, 0f, 0f);

        applyPlayerTransform();
        showRun();
    }

    public void setSpeed(float speed) {
        currentSpeed = Math.min(speed, GameConfig.MAX_PLAYER_SPEED);
    }

    public float getSpeed() {
        return currentSpeed;
    }

    public void snapToTurn(Vector3f cornerPosition, Vector3f newForwardDirection) {
        /*
         * Change direction first.
         */
        forwardDir = newForwardDirection.clone().normalizeLocal();
        rightDir = new Vector3f(-forwardDir.z, 0f, forwardDir.x).normalizeLocal();

        lane = 0;
        laneOffset = 0f;

        /*
         * Move slightly forward on the new path after turn.
         */
        Vector3f safePosition = cornerPosition.add(forwardDir.mult(2.0f));
        centerPosition.set(safePosition.x, 0f, safePosition.z);

        applyPlayerTransform();
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isSliding() {
        return slideTimer > 0f;
    }

    public Vector3f getPosition() {
        return playerNode.getWorldTranslation();
    }

    public int getLane() {
        return lane;
    }

    public Vector3f getForwardDirection() {
        return forwardDir.clone();
    }

    public Vector3f getRightDirection() {
        return rightDir.clone();
    }

    public Spatial getSpatial() {
        return playerNode;
    }
}