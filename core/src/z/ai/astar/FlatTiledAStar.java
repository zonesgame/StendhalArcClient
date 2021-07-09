package z.ai.astar;

import arc.ai.pfa.PathSmoother;
import arc.ai.pfa.indexed.IndexedAStarPathFinder;
import arc.func.Boolf;
import arc.func.Boolf2;
import arc.func.Boolp;
import arc.math.geom.Vec2;
import mindustry.world.Tile;

/**
 *
 */
public class FlatTiledAStar {

    private final boolean smooth = false;

    private FlatTiledGraph worldMap;
//    private TiledSmoothableGraphPath<FlatTiledNode> path;
    private TiledManhattanDistance<FlatTiledNode> heuristic;
    private IndexedAStarPathFinder<FlatTiledNode> pathFinder;
    private PathSmoother<FlatTiledNode, Vec2> pathSmoother;

    public FlatTiledAStar() {
        worldMap = new FlatTiledGraph();

//        path = new TiledSmoothableGraphPath<FlatTiledNode>();
        heuristic = new TiledManhattanDistance<FlatTiledNode>();
    }

    public void initGraph(Tile[][] map, Boolf<Tile> solidCheck) {
        worldMap.init(getAStarMap(map, solidCheck));
        pathFinder = new IndexedAStarPathFinder<FlatTiledNode>(worldMap);
        pathSmoother = new PathSmoother<FlatTiledNode, Vec2>(new TiledRaycastCollisionDetector<FlatTiledNode>(worldMap));
    }

    /***/
    private int[][] getAStarMap(Tile[][] tiles, Boolf<Tile> solidCheck) {
        int width = tiles.length;
        int height = tiles[0].length;
        int[][] map = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = solidCheck.get(tiles[x][y]) ? TiledNode.TILE_WALL : TiledNode.TILE_FLOOR;
            }
        }
        return map;
    }

    public void updateTile(int tilex, int tiley, Boolp cons) {
        worldMap.updateTile(tilex, tiley, cons.get() ? TiledNode.TILE_WALL : TiledNode.TILE_FLOOR);
    }

    /**
     * @param fromX tile value
     * */
    public void getPath (int fromX, int fromY, int targetX, int targetY, TiledSmoothableGraphPath<FlatTiledNode> path) {
        if (fromX != targetX || fromY != targetY) {
            FlatTiledNode startNode = worldMap.getNode(fromX, fromY);
            FlatTiledNode endNode = worldMap.getNode(targetX, targetY);
            if (endNode.type == FlatTiledNode.TILE_FLOOR) {
                path.clear();
                worldMap.startNode = startNode;
                pathFinder.searchNodePath(startNode, endNode, heuristic, path);
                if (smooth) {
                    pathSmoother.smoothPath(path);
                }
            }
        }
    }

    public <N extends TiledNode> void getPathNoLastNode (int fromX, int fromY, int targetX, int targetY, TiledSmoothableGraphPath<FlatTiledNode> path, Boolf2<N, N> checkOver) {
        if (fromX != targetX || fromY != targetY) {
            FlatTiledNode startNode = worldMap.getNode(fromX, fromY);
            FlatTiledNode endNode = worldMap.getNode(targetX, targetY);
            {
                path.clear();
                worldMap.startNode = startNode;
                pathFinder.searchNodePathNoLast(startNode, endNode, heuristic, path, checkOver);
                if (smooth) {
                    pathSmoother.smoothPath(path);
                }
            }
        }
    }

    // zones add begon
    public FlatTiledNode getGraphNode(int x, int y) {
        return worldMap.getNode(x, y);
    }
    // zones add end

}
