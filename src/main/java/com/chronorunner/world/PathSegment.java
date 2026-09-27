package com.chronorunner.world;

import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.List;

public class PathSegment {
    public final Node node = new Node("PathSegment");
    public final float startZ;
    public final float endZ;
    public final SegmentType type;

    public final List<Spatial> coins = new ArrayList<>();
    public final List<Spatial> obstacles = new ArrayList<>();

    public PathSegment(float startZ, float endZ, SegmentType type) {
        this.startZ = startZ;
        this.endZ = endZ;
        this.type = type;
    }

    public void remove() {
        node.removeFromParent();
    }
}
