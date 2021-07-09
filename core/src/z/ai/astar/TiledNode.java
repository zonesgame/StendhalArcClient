package z.ai.astar;

import arc.ai.pfa.Connection;
import arc.math.geom.Vec2;
import arc.struct.Array;

/** A node for a {@link TiledGraph}.
 *
 * @param <N> Type of node, either flat or hierarchical, extending the {@link TiledNode} class
 *
 * @author davebaol */
public abstract class TiledNode<N extends TiledNode<N>> {

    /** A constant representing an empty tile */
    public static final int TILE_EMPTY = 0;

    /** A constant representing a walkable tile */
    public static final int TILE_FLOOR = 1;

    /** A constant representing a wall */
    public static final int TILE_WALL = 2;

    /** The x coordinate of this tile */
    public final int x;

    /** The y coordinate of this tile */
    public final int y;

    /** The type of this tile, see {@link #TILE_EMPTY}, {@link #TILE_FLOOR} and {@link #TILE_WALL} */
    public int type;        // ont final

    protected Array<Connection<N>> connections;

    // zones add begon
//    public Vec2 pos = new Vec2();
    // zones add end

    public TiledNode (int x, int y, int type, Array<Connection<N>> connections) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.connections = connections;

//        this.pos.set(x, y);
    }

    public abstract int getIndex ();

    public Array<Connection<N>> getConnections () {
        return this.connections;
    }

    // zones add function begon
    public Vec2 getPosition() {
        return Vec2.TEMP.set(x, y);
    }

    /** caesar 巡逻获取随机移动节点*/
//    public Connection<N> get(Connection<N> from) {
//        return connections.random(from);
//    }
    // zones add end
}
