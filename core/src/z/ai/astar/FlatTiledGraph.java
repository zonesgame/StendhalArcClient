package z.ai.astar;

import arc.ai.pfa.Connection;
import arc.struct.Array;
import arc.util.Structs;
import mindustry.world.Pos;

/** A random generated graph representing a flat tiled map.
 *
 * @author davebaol */
public class FlatTiledGraph implements TiledGraph<FlatTiledNode> {
    public static int sizeX = 0; // 200; //100;
    public static int sizeY = 0; // 120; //60;

    protected Array<FlatTiledNode> nodes;
    @Deprecated
    private int[][] tempMap = null;

    /** 对角线移动*/
    public final boolean diagonal = false;
    public FlatTiledNode startNode;

    public FlatTiledGraph () {
//        this.diagonal = false;
        this.startNode = null;
    }

    @Override
    public void init (int[][] map) {
        this.tempMap = map;
        sizeX = map.length;
        sizeY = map[0].length;
        this.nodes = new Array<FlatTiledNode>(sizeX * sizeY);
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                nodes.add(new FlatTiledNode(x, y, map[x][y], 4));
            }
        }

        // Each node has up to 4 neighbors, therefore no diagonal movement is possible
        for (int x = 0; x < sizeX; x++) {
            int idx = x * sizeY;
            for (int y = 0; y < sizeY; y++) {
                FlatTiledNode n = nodes.get(idx + y);
                if (x > 0) addConnection(n, -1, 0);
                if (y > 0) addConnection(n, 0, -1);
                if (x < sizeX - 1) addConnection(n, 1, 0);
                if (y < sizeY - 1) addConnection(n, 0, 1);
            }
        }
    }

    @Override
    public FlatTiledNode getNode (int x, int y) {
        if (Structs.inBounds(x, y, tempMap))           // x >= 0 && y >= 0 && x < sizeX && y < sizeY
            return nodes.get(x * sizeY + y);
        return null;
    }

    @Override
    public FlatTiledNode getNode (int index) {
        return nodes.get(index);
    }

    @Override
    public int getIndex (FlatTiledNode node) {
        return node.getIndex();
    }

    @Override
    public int getNodeCount () {
        return nodes.size;
    }

    @Override
    public Array<Connection<FlatTiledNode>> getConnections (FlatTiledNode fromNode) {
        return fromNode.getConnections();
    }

    private void addConnection (FlatTiledNode n, int xOffset, int yOffset) {
        FlatTiledNode target = getNode(n.x + xOffset, n.y + yOffset);
        if (target.type == FlatTiledNode.TILE_FLOOR) n.getConnections().add(new FlatTiledConnection(this, n, target));
    }

//    private Array<Connection<FlatTiledNode>> tempRemoveArray = new Array<>(4);
//    private void removeConnection (FlatTiledNode n, int xOffset, int yOffset) {
//        FlatTiledNode target = getNode(n.x + xOffset, n.y + yOffset);
//
//        for (Connection<FlatTiledNode> connection : n.getConnections()) {
//            if (connection.getFromNode().getIndex() == n.getIndex())
//                tempRemoveArray.add(connection);
//        }
//
//        if (tempRemoveArray.size > 0) {
//            n.getConnections().removeAll(tempRemoveArray);
//            tempRemoveArray.clear();
//        }
//    }

    protected void updateTile(int pos, int tileType) {
        updateTile(Pos.x(pos), Pos.y(pos), tileType);
    }

    protected void updateTile(int x, int y, int tileType) {
        tempMap[x][y] = tileType;
        FlatTiledNode n = nodes.get(x * sizeY + y);
        n.type = tileType;
        if (x > 0) updateTileConnection( x -1, y);
        if (y > 0) updateTileConnection( x, y -1);
        if (x < sizeX - 1) updateTileConnection( x + 1, y);
        if (y < sizeY - 1) updateTileConnection( x, y + 1);
    }

    private void updateTileConnection(int x, int y) {
        FlatTiledNode n = nodes.get(x * sizeY + y);
        n.getConnections().clear();
        if (x > 0) addConnection(n, -1, 0);
        if (y > 0) addConnection(n, 0, -1);
        if (x < sizeX - 1) addConnection(n, 1, 0);
        if (y < sizeY - 1) addConnection(n, 0, 1);
    }

}
