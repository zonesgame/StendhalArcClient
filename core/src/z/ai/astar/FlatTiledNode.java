package z.ai.astar;

import arc.ai.pfa.Connection;
import arc.struct.Array;
import mindustry.Vars;
import mindustry.world.Tile;
import z.world.blocks.distribution.RoadBlockIso;

/** A node for a {@link FlatTiledGraph}.
 *
 * @author davebaol */
public class FlatTiledNode extends TiledNode<FlatTiledNode> {

    public FlatTiledNode (int x, int y, int type, int connectionCapacity) {
        super(x, y, type, new Array<Connection<FlatTiledNode>>(connectionCapacity));
    }

    @Override
    public int getIndex () {
        return x * FlatTiledGraph.sizeY + y;
    }

    @Override
    public boolean equals(Object obj) {
        if ( !(obj instanceof FlatTiledNode)) return false;

        FlatTiledNode otherObj = (FlatTiledNode) obj;
        return Math.abs(otherObj.x - this.x) <= 1 && Math.abs(otherObj.y - this.y) <= 1;
    }

    // zones add begon
    public Connection<FlatTiledNode> getConnection (Connection from) {
//        if(from == null) return connections.random();
//        if(connections.size == 0) return null;
//        if(connections.size == 1) return connections.first();
//
//        Connection<FlatTiledNode> to = null;
//        do {
//            to = connections.random();
//        } while (from.getToNode() == to.getFromNode() && from.getFromNode() == to.getToNode()) ;
//
//        return to;


        if(connections.size == 0) return null;
        if(connections.size == 1) {
            if (targetIsRoadBlock(connections.first().getToNode()))
                return null;
            else
                return connections.first();
        }

        boolean isbreak = true;
        for (Connection<FlatTiledNode> connection : connections) {
            if ( !targetIsRoadBlock(connection.getToNode())) {
                isbreak = false;
                break;
            }
        }

        if (isbreak)    return null;

        Connection<FlatTiledNode> to = null;
        for (int i = 0; ++i < 20; ) {
            to = connections.random();
            if (targetIsRoadBlock(to.getToNode()))
                continue;

            if (from == null ||
                    !(from.getToNode() == to.getFromNode() && from.getFromNode() == to.getToNode()))
                return to;
        }

        return null;
    }

    private boolean targetIsRoadBlock(TiledNode node) {
        Tile tile = Vars.world.tile(node.x, node.y);
        if (tile != null && tile.block() != null && tile.block() instanceof RoadBlockIso)
            return true;
        return false;
    }
    // zones add end
}