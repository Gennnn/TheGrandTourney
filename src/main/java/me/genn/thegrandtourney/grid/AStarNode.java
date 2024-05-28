package me.genn.thegrandtourney.grid;

import java.util.ArrayList;
import java.util.List;

public class AStarNode {
    List<Cell> cells = new ArrayList<>();
    AStarNode parent = null;
    int x;
    int z;
    float g = 0;
    float h = 0;
    float f = 0;
    boolean navigatable;

    public AStarNode(List<Cell> cells,int x, int z, boolean navigatable) {
        this.cells.addAll(cells);
        this.x = x;
        this.z = z;
        this.navigatable = navigatable;
    }
    public AStarNode(List<Cell> cells,int x, int z, boolean navigatable, AStarNode parent) {
        this.cells.addAll(cells);
        this.x = x;
        this.z = z;
        this.navigatable = navigatable;
        this.parent = parent;
    }

    public void reset() {
        this.g = (int) 0;
        this.h = (int) 0;
        this.f = (int) 0;
        this.parent = null;
    }
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AStarNode)) {
            return false;
        }
        AStarNode other = (AStarNode) o;
        if (other.x == this.x && other.z == this.z) {
            return true;
        }
        return false;
    }

    public static boolean contains(List<AStarNode> nodes, AStarNode node) {
        return (nodes.stream().anyMatch(o -> (o.x == node.x && o.z == node.z)));
    }

    public static AStarNode getNode(List<AStarNode>nodes , int x, int z) {
        return (nodes.stream().filter(o -> (o.x == x && o.z == z)).findFirst().orElse(null));
    }


}
