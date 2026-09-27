package com.chronorunner.util;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingVolume;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;

public final class SceneUtil {
    private SceneUtil() {}

    public static void addBasicLights(Node rootNode) {
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.45f, 0.35f, 0.32f, 1f));
        rootNode.addLight(ambient);

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1f, -2f, -1f).normalizeLocal());
        sun.setColor(new ColorRGBA(1f, 0.75f, 0.55f, 1f));
        rootNode.addLight(sun);
    }

    public static Material makeColorMaterial(AssetManager assetManager, ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", color);
        return mat;
    }

    public static Geometry makeBox(String name, AssetManager assetManager, Vector3f halfExtents, ColorRGBA color) {
        Geometry geo = new Geometry(name, new Box(halfExtents.x, halfExtents.y, halfExtents.z));
        geo.setMaterial(makeColorMaterial(assetManager, color));
        return geo;
    }

    public static Spatial safeLoadModel(AssetManager assetManager, String path, Spatial fallback) {
        try {
            return assetManager.loadModel(path);
        } catch (Exception ex) {
            System.out.println("Missing or failed model: " + path + " -> using fallback.");
            return fallback;
        }
    }

    public static boolean intersects(Spatial a, Spatial b) {
        if (a == null || b == null || a.getWorldBound() == null || b.getWorldBound() == null) {
            return false;
        }
        BoundingVolume av = a.getWorldBound();
        BoundingVolume bv = b.getWorldBound();
        return av.intersects(bv);
    }
}
