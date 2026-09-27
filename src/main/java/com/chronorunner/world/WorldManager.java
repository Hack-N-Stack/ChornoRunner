package com.chronorunner.world;

import com.chronorunner.AssetPaths;
import com.chronorunner.GameConfig;
import com.chronorunner.player.PlayerController;
import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

import java.util.*;

public class WorldManager {

    public interface CoinListener {
        void onCoinCollected();
    }

    public interface DeathListener {
        void onPlayerHit();
    }

    public enum TurnInput {
        LEFT,
        RIGHT
    }

    private enum ObstacleType {
        ROCK,
        GAP,
        LOW_CEILING,
        BROKEN_PATH
    }

    private enum TurnDirection {
        LEFT,
        RIGHT
    }

    private static class ObstacleInfo {
        ObstacleType type;
        int lane;
        Vector3f position;
        Vector3f forward;
        float range;

        ObstacleInfo(ObstacleType type, int lane, Vector3f position, Vector3f forward, float range) {
            this.type = type;
            this.lane = lane;
            this.position = position;
            this.forward = forward.normalize();
            this.range = range;
        }
    }

    private static class SegmentData {
        Node node = new Node("Segment");
        Vector3f center;
        Vector3f forward;
        Vector3f right;
        List<Spatial> coins = new ArrayList<>();
        List<ObstacleInfo> obstacles = new ArrayList<>();
    }

    private final AssetManager assetManager;
    private final Node rootNode;

    private final Node worldNode = new Node("ProperTurnVolcanoWorld");
    private final Node staticNode = new Node("MovingBackground");
    private final Node segmentsNode = new Node("Segments");

    private final Queue<SegmentData> activeSegments = new LinkedList<>();
    private final Random random = new Random();

    private Vector3f nextSegmentStart = new Vector3f(0f, 0f, 20f);
    private Vector3f spawnForward = new Vector3f(0f, 0f, -1f);
    private Vector3f spawnRight = new Vector3f(1f, 0f, 0f);

    private int segmentIndex = 0;
    private int nextTurnSegmentIndex = 12;

    private float elapsedTime = 0f;
    private int difficultyLevel = 0;

    private boolean activeTurn = false;
    private boolean activeTurnCompleted = false;

    private Vector3f activeTurnPosition = new Vector3f();
    private Vector3f activeTurnOldForward = new Vector3f(0, 0, -1);
    private Vector3f activeTurnNewForward = new Vector3f();

    private TurnDirection activeTurnDirection = TurnDirection.LEFT;

    private static final float PATH_MODEL_Y = 0.05f;
    private static final float PATH_SURFACE_Y = 0.62f;

    private Material skyMat;
    private Material darkRockMat;
    private Material pathMat;
    private Material lavaMat;
    private Material lavaBrightMat;
    private Material coinMat;
    private Material lavaChunkSurfaceMat;
    private Material lavaSurfaceMat;
    private Material crackedRockSurfaceMat;
    private Material turnLeftMat;
    private Material turnRightMat;
    private Material warningMat;

    private Spatial lavaCubeTemplate;
    private Spatial lavaRockTemplate;
    private Spatial crackedRockTemplate;
    private Spatial caveRockTemplate;
    private Spatial lavaChunkTemplate;
    private Spatial floorModularTemplate;

    private Spatial skyTemplate;
    private Spatial volcanoTemplate;

    private Spatial activeSky;
    private Spatial activeVolcano;
    private Geometry activeLavaOcean;

    public WorldManager(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;

        rootNode.attachChild(worldNode);
        worldNode.attachChild(staticNode);
        worldNode.attachChild(segmentsNode);

        createMaterials();
        loadTemplates();
        buildMovingBackground();
    }

    private void createMaterials() {
        skyMat = makeMat(new ColorRGBA(0.025f, 0.030f, 0.055f, 1f));
        darkRockMat = makeMat(new ColorRGBA(0.12f, 0.10f, 0.09f, 1f));
        pathMat = makeMat(new ColorRGBA(0.30f, 0.27f, 0.24f, 1f));

        lavaMat = makeMat(new ColorRGBA(0.82f, 0.14f, 0.02f, 1f));
        lavaBrightMat = makeMat(new ColorRGBA(0.96f, 0.30f, 0.05f, 1f));

        coinMat = makeMat(new ColorRGBA(1.00f, 0.88f, 0.08f, 1f));

        turnLeftMat = makeMat(new ColorRGBA(0.15f, 0.45f, 1f, 1f));
        turnRightMat = makeMat(new ColorRGBA(0.25f, 1f, 0.45f, 1f));
        warningMat = makeMat(new ColorRGBA(1f, 0.12f, 0.05f, 1f));
    }

    private Material makeMat(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }

    private void loadTemplates() {
        lavaCubeTemplate = safeLoad(AssetPaths.LAVA_CUBE);
        lavaRockTemplate = safeLoad(AssetPaths.LAVA_ROCK);
        crackedRockTemplate = safeLoad(AssetPaths.CRACKED_ROCKS);
        caveRockTemplate = safeLoad(AssetPaths.CAVE_ROCKS);
        lavaChunkTemplate = safeLoad(AssetPaths.LAVA_CHUNK);
        floorModularTemplate = safeLoad(AssetPaths.FLOOR_MODULAR);

        skyTemplate = safeLoad(AssetPaths.SKY);
        volcanoTemplate = safeLoad(AssetPaths.VOLCANO);

        if (lavaChunkTemplate != null) {
            lavaChunkSurfaceMat = extractFirstMaterial(lavaChunkTemplate);
        }

        if (lavaCubeTemplate != null) {
            lavaSurfaceMat = extractFirstMaterial(lavaCubeTemplate);
        } else if (lavaChunkSurfaceMat != null) {
            lavaSurfaceMat = lavaChunkSurfaceMat;
        }

        if (crackedRockTemplate != null) {
            crackedRockSurfaceMat = extractFirstMaterial(crackedRockTemplate);
        }
    }

    private Spatial safeLoad(String path) {
        try {
            Spatial model = assetManager.loadModel(path);
            System.out.println("Loaded environment asset: " + path);
            return model;
        } catch (Exception ex) {
            System.out.println("Could not load environment asset: " + path);
            System.out.println("Reason: " + ex.getMessage());
            return null;
        }
    }

    private Material extractFirstMaterial(Spatial spatial) {
        if (spatial == null) return null;

        if (spatial instanceof Geometry geometry && geometry.getMaterial() != null) {
            return geometry.getMaterial().clone();
        }

        if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                Material found = extractFirstMaterial(child);
                if (found != null) return found;
            }
        }

        return null;
    }

    private void buildMovingBackground() {
        if (skyTemplate != null) {
            activeSky = skyTemplate.clone();
            activeSky.setLocalScale(85f);
            activeSky.setLocalTranslation(0f, -8f, -180f);
            staticNode.attachChild(activeSky);
        } else {
            Geometry fallbackSky = box("FallbackDarkSky", 90f, 45f, 1f, skyMat);
            fallbackSky.setLocalTranslation(0, 24f, -180f);
            staticNode.attachChild(fallbackSky);
        }

        if (volcanoTemplate != null) {
            activeVolcano = volcanoTemplate.clone();
            activeVolcano.setLocalScale(7.5f);
            activeVolcano.setLocalTranslation(0f, -5f, -280f);
            staticNode.attachChild(activeVolcano);
        }

        activeLavaOcean = lavaSurfaceBox("MovingLavaOcean", 42f, 0.08f, 260f);
        activeLavaOcean.setLocalTranslation(0, -2.2f, -110f);
        staticNode.attachChild(activeLavaOcean);

        addLight(new Vector3f(0, 7, 5), new ColorRGBA(1f, 0.34f, 0.08f, 1f), 35f);
        addLight(new Vector3f(0, 12, -90), new ColorRGBA(1f, 0.22f, 0.05f, 1f), 60f);
        addLight(new Vector3f(0, 16, -190), new ColorRGBA(1f, 0.18f, 0.04f, 1f), 75f);
    }

    private void updateMovingBackground(PlayerController player) {
        Vector3f p = player.getPosition();
        Vector3f f = player.getForwardDirection();

        if (activeSky != null) {
            activeSky.setLocalTranslation(p.x + f.x * 180f, -8f, p.z + f.z * 180f);
        }

        if (activeVolcano != null) {
            activeVolcano.setLocalTranslation(p.x + f.x * 280f, -5f, p.z + f.z * 280f);
        }

        if (activeLavaOcean != null) {
            activeLavaOcean.setLocalTranslation(p.x + f.x * 110f, -2.2f, p.z + f.z * 110f);
        }
    }

    private void addLight(Vector3f position, ColorRGBA color, float radius) {
        PointLight light = new PointLight();
        light.setPosition(position);
        light.setColor(color);
        light.setRadius(radius);
        worldNode.addLight(light);
    }

    public void startWorld() {
        clear();

        elapsedTime = 0f;
        difficultyLevel = 0;

        activeTurn = false;
        activeTurnCompleted = false;

        nextSegmentStart.set(0f, 0f, 20f);
        spawnForward.set(0f, 0f, -1f);
        spawnRight.set(1f, 0f, 0f);

        segmentIndex = 0;
        nextTurnSegmentIndex = 12;

        for (int i = 0; i < GameConfig.START_SEGMENTS; i++) {
            spawnSegment();
        }
    }

    public void clear() {
        while (!activeSegments.isEmpty()) {
            activeSegments.poll().node.removeFromParent();
        }
    }

    public float getCurrentSpeed() {
        return Math.min(
                GameConfig.START_SPEED + difficultyLevel * GameConfig.SPEED_INCREASE_AFTER_TURN,
                GameConfig.MAX_PLAYER_SPEED
        );
    }

    public boolean isPlayerInTurnWindow(PlayerController player) {
        if (!activeTurn || player == null) return false;
        return player.getPosition().distance(activeTurnPosition) <= GameConfig.TURN_INPUT_RANGE;
    }

    public String getRequiredTurnText() {
        if (!activeTurn) return "";
        return activeTurnDirection == TurnDirection.LEFT ? "LEFT" : "RIGHT";
    }

    public boolean handleTurnInput(TurnInput input, PlayerController player) {
        if (!isPlayerInTurnWindow(player)) return false;

        boolean correct =
                (input == TurnInput.LEFT && activeTurnDirection == TurnDirection.LEFT)
                        || (input == TurnInput.RIGHT && activeTurnDirection == TurnDirection.RIGHT);

        if (!correct) return false;

        activeTurnCompleted = true;
        activeTurn = false;

        player.snapToTurn(activeTurnPosition, activeTurnNewForward);

        difficultyLevel++;
        nextTurnSegmentIndex = segmentIndex + 10;

        System.out.println("Proper turn completed. New difficulty: " + difficultyLevel);
        return true;
    }

    public void update(float tpf, PlayerController player, CoinListener coinListener, DeathListener deathListener) {
        elapsedTime += tpf;

        updateMovingBackground(player);

        while (!activeSegments.isEmpty()
                && activeSegments.peek().center.distance(player.getPosition()) > 150f) {
            activeSegments.poll().node.removeFromParent();
        }

        while (activeSegments.size() < GameConfig.START_SEGMENTS) {
            spawnSegment();
        }

        checkMissedTurn(player, deathListener);
        checkCoins(player, coinListener);
        checkObstacles(player, deathListener);
        animateCoins();
    }

    private void checkMissedTurn(PlayerController player, DeathListener deathListener) {
        if (!activeTurn || activeTurnCompleted) return;

        Vector3f diff = player.getPosition().subtract(activeTurnPosition);
        float passedAmount = diff.dot(activeTurnOldForward);

        if (passedAmount > GameConfig.TURN_MISS_DISTANCE) {
            deathListener.onPlayerHit();
        }
    }

    private void spawnSegment() {
        if (!activeTurn && segmentIndex >= nextTurnSegmentIndex) {
            spawnTurnSegment();
            return;
        }

        SegmentData seg = new SegmentData();

        Vector3f start = nextSegmentStart.clone();
        Vector3f end = start.add(spawnForward.mult(GameConfig.SEGMENT_LENGTH));
        Vector3f center = start.add(spawnForward.mult(GameConfig.SEGMENT_LENGTH / 2f));

        seg.center = center;
        seg.forward = spawnForward.clone();
        seg.right = spawnRight.clone();

        int segmentsUntilTurn = nextTurnSegmentIndex - segmentIndex;
        boolean turnIsNear = segmentsUntilTurn <= 3 || activeTurn;

        boolean makeJumpGapSegment = false;

        if (segmentIndex >= 4 && !turnIsNear) {
            int jumpGapChance = difficultyLevel >= 1 ? 18 : 10;
            makeJumpGapSegment = random.nextInt(100) < jumpGapChance;
        }

        buildLavaUnderSegment(seg, center, seg.forward);

        if (makeJumpGapSegment) {
            buildStraightPathWithJumpGap(seg, center, seg.forward);
            decorateFarSides(seg, center, seg.forward, seg.right);
        } else {
            buildStraightPath(seg, center, seg.forward);
            decorateFarSides(seg, center, seg.forward, seg.right);

            if (segmentIndex < 3) {
                addCoins(seg);
            } else {
                if (turnIsNear) {
                    addGameplayWithoutGaps(seg);
                } else {
                    addGameplay(seg);
                }
            }
        }

        segmentsNode.attachChild(seg.node);
        activeSegments.add(seg);

        nextSegmentStart = end;
        segmentIndex++;
    }

    private void spawnTurnSegment() {
        SegmentData seg = new SegmentData();

        Vector3f start = nextSegmentStart.clone();

        activeTurnDirection = random.nextBoolean() ? TurnDirection.LEFT : TurnDirection.RIGHT;
        activeTurnOldForward = spawnForward.clone();

        Vector3f newForward = activeTurnDirection == TurnDirection.LEFT
                ? rotateLeft(spawnForward)
                : rotateRight(spawnForward);

        Vector3f newRight = rightFromForward(newForward);

        Vector3f corner = start.add(spawnForward.mult(GameConfig.SEGMENT_LENGTH * 0.65f));
        Vector3f exit = corner.add(newForward.mult(GameConfig.SEGMENT_LENGTH * 0.95f));
        Vector3f center = start.add(exit).multLocal(0.5f);

        activeTurnPosition = corner.clone();
        activeTurnNewForward = newForward.clone();
        activeTurn = true;
        activeTurnCompleted = false;

        seg.center = center;
        seg.forward = spawnForward.clone();
        seg.right = spawnRight.clone();

        buildLavaUnderSegment(seg, center, spawnForward);

        /*
         * Tightly filled incoming path.
         */
        Vector3f incomingStart = start.add(spawnForward.mult(2f));
        attachPathStrip(seg.node, incomingStart, spawnForward, 7, 3.5f);

        Geometry cornerBase = crackedRockBox("CornerRockPlatform", 3.8f, 0.15f, 3.8f);
        placeSpatial(cornerBase, corner, PATH_SURFACE_Y + 0.02f, spawnForward);
        seg.node.attachChild(cornerBase);

        /*
         * Tightly filled outgoing path.
         */
        Vector3f outgoingStart = corner.add(newForward.mult(2f));
        attachPathStrip(seg.node, outgoingStart, newForward, 9, 3.5f);

        addTurnArrow(seg.node, corner, spawnForward, newForward, activeTurnDirection);
        decorateFarSides(seg, center, spawnForward, spawnRight);

        segmentsNode.attachChild(seg.node);
        activeSegments.add(seg);

        spawnForward = newForward;
        spawnRight = newRight;
        nextSegmentStart = exit;

        segmentIndex++;

        System.out.println("Proper path turn spawned: " + activeTurnDirection);
    }

    private void buildLavaUnderSegment(SegmentData seg, Vector3f center, Vector3f forward) {
        Geometry lava = lavaSurfaceBox("SegmentLava", 18f, 0.06f, GameConfig.SEGMENT_LENGTH / 2f + 2f);
        placeSpatial(lava, center, -1.35f, forward);
        seg.node.attachChild(lava);
    }

    private void buildStraightPath(SegmentData seg, Vector3f center, Vector3f forward) {
        /*
         * Tightly filled normal path.
         * This removes unwanted random visual spaces where player could pass smoothly.
         */
        Vector3f stripStart = center.add(forward.mult(-16f));
        attachPathStrip(seg.node, stripStart, forward, 9, 3.5f);

        for (int i = 0; i < 3; i++) {
            Vector3f pos = pointOnPath(
                    center,
                    forward,
                    rightFromForward(forward),
                    randomRange(-15f, 15f),
                    randomRange(-2f, 2f)
            );

            Geometry crack = lavaChunkPlate(
                    "LavaCrack",
                    randomRange(0.06f, 0.13f),
                    0.02f,
                    randomRange(0.8f, 2.0f)
            );

            placeSpatial(crack, pos, PATH_SURFACE_Y + 0.04f, forward);
            crack.rotate(0, randomRange(0f, FastMath.PI), 0);
            seg.node.attachChild(crack);
        }
    }

    private void buildStraightPathWithJumpGap(SegmentData seg, Vector3f center, Vector3f forward) {
        /*
         * Tightly filled path leading up to the jump gap.
         * Using 3 tiles spaced by 4.0f starting at -16f puts the last tile's front edge exactly at -2.0f,
         * perfectly overlapping with the gap's back edge rock to leave no running exploits.
         */
        Vector3f backStripStart = center.add(forward.mult(-16f));
        attachPathStrip(seg.node, backStripStart, forward, 3, 4.0f);

        /*
         * Tightly filled path continuing after the jump gap.
         * Starting at 8.0f puts the first tile's back edge exactly at +2.0f,
         * perfectly matching the front edge rock.
         */
        Vector3f frontStripStart = center.add(forward.mult(8.0f));
        attachPathStrip(seg.node, frontStripStart, forward, 3, 4.0f);

        Geometry frontEdge = crackedRockBox("CleanGapFrontEdge", 4.5f, 0.10f, 0.16f);
        placeSpatial(frontEdge, center.add(forward.mult(2.05f)), PATH_SURFACE_Y + 0.10f, forward);
        seg.node.attachChild(frontEdge);

        Geometry backEdge = crackedRockBox("CleanGapBackEdge", 4.5f, 0.10f, 0.16f);
        placeSpatial(backEdge, center.add(forward.mult(-2.05f)), PATH_SURFACE_Y + 0.10f, forward);
        seg.node.attachChild(backEdge);

        seg.obstacles.add(new ObstacleInfo(ObstacleType.BROKEN_PATH, 99, center, forward, 0.95f));
    }
    private void attachPathStrip(Node parent, Vector3f start, Vector3f forward, int count, float spacing) {
        for (int i = 0; i < count; i++) {
            Vector3f pos = start.add(forward.mult(i * spacing));
            attachPathModel(parent, pos, PATH_MODEL_Y, forward, 5.2f, 12f);
        }
    }

    private void attachPathModel(Node parent, Vector3f position, float y, Vector3f forward, float targetWidth, float targetLength) {
        if (floorModularTemplate == null) {
            Geometry fallbackPath = box("FallbackPathOnlyIfFloorMissing", 5.2f, 0.18f, targetLength / 2f, pathMat);
            placeSpatial(fallbackPath, position, y, forward);
            parent.attachChild(fallbackPath);
            return;
        }

        try {
            Spatial clone = floorModularTemplate.clone();

            clone.setLocalScale(1f);
            clone.setLocalTranslation(0, 0, 0);
            clone.updateGeometricState();

            float maxExtent = getMaxExtent(clone);

            if (maxExtent <= 0.001f) {
                maxExtent = 1f;
            }

            float scale = Math.min(targetWidth, targetLength) / maxExtent;

            clone.setLocalScale(scale);
            placeSpatial(clone, position, y, forward);

            parent.attachChild(clone);

        } catch (Exception ex) {
            System.out.println("Could not attach Floor_Modular model.");
            System.out.println("Reason: " + ex.getMessage());
        }
    }

    private void addTurnArrow(Node parent, Vector3f corner, Vector3f oldForward, Vector3f newForward, TurnDirection direction) {
        Material arrowMat = direction == TurnDirection.LEFT ? turnLeftMat : turnRightMat;
        Vector3f oldRight = rightFromForward(oldForward);

        Geometry warningLine = box("TurnWarningLine", 4.8f, 0.05f, 0.16f, warningMat);
        placeSpatial(warningLine, corner.subtract(oldForward.mult(4f)), PATH_SURFACE_Y + 0.12f, oldForward);
        parent.attachChild(warningLine);

        Geometry arrowBody = box("TurnArrowBody", 1.4f, 0.08f, 0.22f, arrowMat);
        Vector3f arrowBodyPos = corner.add(newForward.mult(1.1f));
        placeSpatial(arrowBody, arrowBodyPos, PATH_SURFACE_Y + 0.65f, newForward);
        parent.attachChild(arrowBody);

        Geometry arrowHeadA = box("TurnArrowHeadA", 0.55f, 0.08f, 0.20f, arrowMat);
        Geometry arrowHeadB = box("TurnArrowHeadB", 0.55f, 0.08f, 0.20f, arrowMat);

        Vector3f headCenter = corner.add(newForward.mult(2.2f));

        placeSpatial(arrowHeadA, headCenter.add(oldRight.mult(0.35f)), PATH_SURFACE_Y + 0.65f, newForward);
        placeSpatial(arrowHeadB, headCenter.add(oldRight.mult(-0.35f)), PATH_SURFACE_Y + 0.65f, newForward);

        arrowHeadA.rotate(0, FastMath.QUARTER_PI, 0);
        arrowHeadB.rotate(0, -FastMath.QUARTER_PI, 0);

        parent.attachChild(arrowHeadA);
        parent.attachChild(arrowHeadB);
    }

    private void decorateFarSides(SegmentData seg, Vector3f center, Vector3f forward, Vector3f right) {
        for (int i = 0; i < 5; i++) {
            float side = random.nextBoolean() ? -1f : 1f;

            Vector3f pos = center
                    .add(right.mult(side * randomRange(9f, 16f)))
                    .add(forward.mult(randomRange(-15f, 15f)));

            attachNormalizedTemplate(
                    seg.node,
                    chooseTemplate(),
                    randomRange(0.8f, 1.7f),
                    pos.x,
                    randomRange(-0.8f, 1.3f),
                    pos.z
            );
        }
    }

    private void addGameplay(SegmentData seg) {
        int r = random.nextInt(100);

        int rockChance = difficultyLevel >= 1 ? 32 : 26;
        int lavaChance = difficultyLevel >= 1 ? 58 : 48;
        int hurdleChance = difficultyLevel >= 1 ? 86 : 76;

        if (r < rockChance) {
            addRockObstacle(seg, randomLane());
        } else if (r < lavaChance) {
            addGap(seg);
        } else if (r < hurdleChance) {
            addLowCeiling(seg);
        } else {
            addCoins(seg);
        }
    }

    private void addGameplayWithoutGaps(SegmentData seg) {
        int r = random.nextInt(100);

        int rockChance = difficultyLevel >= 1 ? 45 : 40;
        int hurdleChance = difficultyLevel >= 1 ? 80 : 70;

        if (r < rockChance) {
            addRockObstacle(seg, randomLane());
        } else if (r < hurdleChance) {
            addLowCeiling(seg);
        } else {
            addCoins(seg);
        }
    }

    private void addCoins(SegmentData seg) {
        int lane = randomLane();
        Vector3f zStart = seg.center.add(seg.forward.mult(-8f));

        for (int i = 0; i < 5; i++) {
            Vector3f pos = zStart
                    .add(seg.forward.mult(-i * 4f))
                    .add(seg.right.mult(lane * GameConfig.LANE_WIDTH));

            Geometry coin = box("Coin", 0.22f, 0.34f, 0.08f, coinMat);
            placeSpatial(coin, pos, PATH_SURFACE_Y + 1.25f, seg.forward);

            seg.coins.add(coin);
            seg.node.attachChild(coin);
        }
    }

    private void addRockObstacle(SegmentData seg, int lane) {
        Vector3f pos = pointOnPath(seg.center, seg.forward, seg.right, -5f, lane * GameConfig.LANE_WIDTH);

        Spatial obstacle = attachNormalizedTemplate(
                seg.node,
                lavaRockTemplate,
                1.15f,
                pos.x,
                PATH_SURFACE_Y + 0.65f,
                pos.z
        );

        if (obstacle == null) {
            Geometry sphere = new Geometry("RockObstacle", new Sphere(18, 18, 0.8f));
            sphere.setMaterial(darkRockMat);
            placeSpatial(sphere, pos, PATH_SURFACE_Y + 0.65f, seg.forward);
            seg.node.attachChild(sphere);
        }

        Geometry warning = lavaChunkPlate("RockWarning", 1.0f, 0.03f, 1.0f);
        placeSpatial(warning, pos, PATH_SURFACE_Y + 0.06f, seg.forward);
        seg.node.attachChild(warning);

        seg.obstacles.add(new ObstacleInfo(ObstacleType.ROCK, lane, pos, seg.forward, 0.75f));
    }

    private void addGap(SegmentData seg) {
        Vector3f center = pointOnPath(seg.center, seg.forward, seg.right, -5f, 0f);

        float[] laneOffsets = {-3.4f, -2.1f, -0.8f, 0.5f, 1.8f, 3.1f};

        for (float laneOffset : laneOffsets) {
            Vector3f pos = center
                    .add(seg.right.mult(laneOffset + randomRange(-0.15f, 0.15f)))
                    .add(seg.forward.mult(randomRange(-0.35f, 0.35f)));

            Geometry lavaPiece = lavaSurfaceBox(
                    "FullWidthLavaPiece",
                    randomRange(0.55f, 0.95f),
                    0.06f,
                    randomRange(0.75f, 1.25f)
            );

            placeSpatial(lavaPiece, pos, PATH_SURFACE_Y + 0.08f, seg.forward);
            lavaPiece.rotate(0, randomRange(-0.9f, 0.9f), 0);
            seg.node.attachChild(lavaPiece);
        }

        for (int i = 0; i < 4; i++) {
            Vector3f pos = center
                    .add(seg.right.mult(randomRange(-3.5f, 3.5f)))
                    .add(seg.forward.mult(randomRange(-0.9f, 0.9f)));

            Geometry extra = lavaSurfaceBox(
                    "ExtraScatteredLava",
                    randomRange(0.45f, 0.8f),
                    0.05f,
                    randomRange(0.45f, 0.9f)
            );

            placeSpatial(extra, pos, PATH_SURFACE_Y + 0.10f, seg.forward);
            extra.rotate(0, randomRange(-1.2f, 1.2f), 0);
            seg.node.attachChild(extra);
        }

        seg.obstacles.add(new ObstacleInfo(ObstacleType.GAP, 99, center, seg.forward, 0.85f));
    }

    private void addLowCeiling(SegmentData seg) {
        Vector3f center = pointOnPath(seg.center, seg.forward, seg.right, -5f, 0f);

        Geometry beam = crackedRockBox("DualHurdleBeam", 4.8f, 0.32f, 0.45f);
        placeSpatial(beam, center, PATH_SURFACE_Y + 1.45f, seg.forward);
        seg.node.attachChild(beam);

        Geometry leftSupport = crackedRockBox("DualHurdleLeftSupport", 0.22f, 1.10f, 0.22f);
        placeSpatial(leftSupport, center.add(seg.right.mult(-2.25f)), PATH_SURFACE_Y + 0.78f, seg.forward);
        seg.node.attachChild(leftSupport);

        Geometry rightSupport = crackedRockBox("DualHurdleRightSupport", 0.22f, 1.10f, 0.22f);
        placeSpatial(rightSupport, center.add(seg.right.mult(2.25f)), PATH_SURFACE_Y + 0.78f, seg.forward);
        seg.node.attachChild(rightSupport);

        Geometry warning = lavaChunkPlate("DualHurdleWarning", 5.0f, 0.03f, 0.15f);
        placeSpatial(warning, center.add(seg.forward.mult(2.2f)), PATH_SURFACE_Y + 0.04f, seg.forward);
        seg.node.attachChild(warning);

        seg.obstacles.add(new ObstacleInfo(ObstacleType.LOW_CEILING, 99, center, seg.forward, 1.35f));
    }

    private void checkCoins(PlayerController player, CoinListener listener) {
        Vector3f p = player.getPosition();

        for (SegmentData seg : activeSegments) {
            Iterator<Spatial> iterator = seg.coins.iterator();

            while (iterator.hasNext()) {
                Spatial coin = iterator.next();

                if (coin.getWorldTranslation().distance(p) < GameConfig.COIN_PICK_DISTANCE) {
                    coin.removeFromParent();
                    iterator.remove();
                    listener.onCoinCollected();
                }
            }
        }
    }

    private void checkObstacles(PlayerController player, DeathListener listener) {
        Vector3f p = player.getPosition();
        int playerLane = player.getLane();

        for (SegmentData seg : activeSegments) {
            for (ObstacleInfo obstacle : seg.obstacles) {
                boolean sameLane = obstacle.lane == 99 || obstacle.lane == playerLane;

                Vector3f diff = p.subtract(obstacle.position);
                float alongDistance = Math.abs(diff.dot(obstacle.forward));

                boolean close = alongDistance < obstacle.range;

                if (!sameLane || !close) {
                    continue;
                }

                if (obstacle.type == ObstacleType.GAP) {
                    boolean jumpedOver = p.y > 1.55f;

                    if (jumpedOver) {
                        continue;
                    }

                    listener.onPlayerHit();
                    return;
                }

                if (obstacle.type == ObstacleType.BROKEN_PATH) {
                    boolean jumpedOverGap = p.y > 1.52f;

                    if (jumpedOverGap) {
                        continue;
                    }

                    listener.onPlayerHit();
                    return;
                }

                if (obstacle.type == ObstacleType.LOW_CEILING) {
                    boolean jumpedHighEnough = p.y > 1.85f;
                    boolean slidingUnder = player.isSliding();

                    if (jumpedHighEnough || slidingUnder) {
                        continue;
                    }

                    listener.onPlayerHit();
                    return;
                }

                if (obstacle.type == ObstacleType.ROCK) {
                    boolean jumpedOverRock = p.y > 1.55f;

                    if (jumpedOverRock) {
                        continue;
                    }

                    listener.onPlayerHit();
                    return;
                }
            }
        }
    }

    private void animateCoins() {
        for (SegmentData seg : activeSegments) {
            for (Spatial coin : seg.coins) {
                coin.rotate(0, 0.08f, 0);
            }
        }
    }

    private Spatial chooseTemplate() {
        int r = random.nextInt(4);

        if (r == 0) return crackedRockTemplate;
        if (r == 1) return lavaRockTemplate;
        if (r == 2) return caveRockTemplate;
        return lavaChunkTemplate;
    }

    private Spatial attachNormalizedTemplate(Node parent, Spatial template, float targetSize, float x, float y, float z) {
        if (template == null) {
            return null;
        }

        try {
            Spatial clone = template.clone();

            clone.setLocalScale(1f);
            clone.setLocalTranslation(0, 0, 0);
            clone.updateGeometricState();

            float maxExtent = getMaxExtent(clone);

            if (maxExtent <= 0.001f) {
                maxExtent = 1f;
            }

            float scale = targetSize / maxExtent;

            clone.setLocalScale(scale);
            clone.setLocalTranslation(x, y, z);

            clone.rotate(
                    randomRange(0f, 0.15f),
                    randomRange(0f, FastMath.TWO_PI),
                    randomRange(0f, 0.10f)
            );

            parent.attachChild(clone);
            return clone;

        } catch (Exception ex) {
            return null;
        }
    }

    private Vector3f pointOnPath(Vector3f center, Vector3f forward, Vector3f right, float along, float side) {
        return center.add(forward.mult(along)).add(right.mult(side));
    }

    private Vector3f rotateLeft(Vector3f dir) {
        return new Vector3f(dir.z, 0f, -dir.x).normalizeLocal();
    }

    private Vector3f rotateRight(Vector3f dir) {
        return new Vector3f(-dir.z, 0f, dir.x).normalizeLocal();
    }

    private Vector3f rightFromForward(Vector3f forward) {
        return new Vector3f(-forward.z, 0f, forward.x).normalizeLocal();
    }

    private float yawFromForward(Vector3f forward) {
        return FastMath.atan2(forward.x, -forward.z);
    }

    private void placeSpatial(Spatial spatial, Vector3f position, float y, Vector3f forward) {
        spatial.setLocalTranslation(position.x, y, position.z);
        spatial.setLocalRotation(new com.jme3.math.Quaternion().fromAngleAxis(
                yawFromForward(forward),
                Vector3f.UNIT_Y
        ));
    }

    private float getMaxExtent(Spatial spatial) {
        spatial.updateModelBound();
        spatial.updateGeometricState();

        BoundingVolume bound = spatial.getWorldBound();

        if (bound instanceof BoundingBox box) {
            return Math.max(
                    box.getXExtent(),
                    Math.max(box.getYExtent(), box.getZExtent())
            );
        }

        return 1f;
    }

    private Geometry lavaChunkPlate(String name, float x, float y, float z) {
        Geometry g = new Geometry(name, new Box(x, y, z));

        if (lavaChunkSurfaceMat != null) {
            g.setMaterial(lavaChunkSurfaceMat);
        } else {
            g.setMaterial(lavaBrightMat);
        }

        return g;
    }

    private Geometry lavaSurfaceBox(String name, float x, float y, float z) {
        Geometry g = new Geometry(name, new Box(x, y, z));

        if (lavaSurfaceMat != null) {
            g.setMaterial(lavaSurfaceMat);
        } else {
            g.setMaterial(lavaMat);
        }

        return g;
    }

    private Geometry crackedRockBox(String name, float x, float y, float z) {
        Geometry g = new Geometry(name, new Box(x, y, z));

        if (crackedRockSurfaceMat != null) {
            g.setMaterial(crackedRockSurfaceMat);
        } else {
            g.setMaterial(darkRockMat);
        }

        return g;
    }

    private Geometry box(String name, float x, float y, float z, Material mat) {
        Geometry g = new Geometry(name, new Box(x, y, z));
        g.setMaterial(mat);
        return g;
    }

    private int randomLane() {
        return random.nextInt(3) - 1;
    }

    private float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }
}