package z.ai.astar;

import arc.ai.pfa.indexed.IndexedGraph;

/** Graph interface representing a generic tiled map.
 *
 * @param <N> Type of node, either flat or hierarchical, extending the {@link TiledNode} class
 *
 * @author davebaol */
public interface TiledGraph<N extends TiledNode<N>> extends IndexedGraph<N> {

    public void init(int[][] map);

    public N getNode(int x, int y);

    public N getNode(int index);

}
