package mindustry.input;

import java.util.PriorityQueue;

import arc.Core;
import arc.math.Mathf;
import arc.math.geom.Bresenham2;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Array;
import arc.struct.IntFloatMap;
import arc.struct.IntIntMap;
import arc.struct.IntSet;
import arc.util.pooling.Pools;
import mindustry.world.Block;
import mindustry.world.Pos;
import mindustry.world.Tile;

import static mindustry.Vars.control;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class Placement{
    private static final NormalizeResult result = new NormalizeResult();
    private static final NormalizeDrawResult drawResult = new NormalizeDrawResult();
    private static Bresenham2 bres = new Bresenham2();
    private static Array<Point2> points = new Array<>();

    //for pathfinding
    private static IntFloatMap costs = new IntFloatMap();
    private static IntIntMap parents = new IntIntMap();
    private static IntSet closed = new IntSet();

    /** Normalize a diagonal line into points. */
    public static Array<Point2> pathfindLine(boolean conveyors, int startX, int startY, int endX, int endY){
        Pools.freeAll(points);

        points.clear();
        if(conveyors && Core.settings.getBool("conveyorpathfinding")){
            if(astar(startX, startY, endX, endY)){
                return points;
            }else{
                return normalizeLine(startX, startY, endX, endY);
            }
        }else{
            return bres.lineNoDiagonal(startX, startY, endX, endY, Pools.get(Point2.class, Point2::new), points);
        }
    }

    /** Normalize two points into one straight line, no diagonals. */
    public static Array<Point2> normalizeLine(int startX, int startY, int endX, int endY){
        Pools.freeAll(points);
        points.clear();
        if(Math.abs(startX - endX) > Math.abs(startY - endY)){
            //go width
            for(int i = 0; i <= Math.abs(startX - endX); i++){
                points.add(Pools.obtain(Point2.class, Point2::new).set(startX + i * Mathf.sign(endX - startX), startY));
            }
        }else{
            //go height
            for(int i = 0; i <= Math.abs(startY - endY); i++){
                points.add(Pools.obtain(Point2.class, Point2::new).set(startX, startY + i * Mathf.sign(endY - startY)));
            }
        }
        return points;
    }

    private static float tileHeuristic(Tile tile, Tile other){
        Block block = control.input.block;

        if((!other.block().alwaysReplace && !(block != null && block.canReplace(other.block()))) || other.floor().isDeep()){
            return 20;
        }else{
            if(parents.containsKey(tile.pos())){
                Tile prev = world.tile(parents.get(tile.pos(), 0));
                if(tile.relativeTo(prev) != other.relativeTo(tile)){
                    return 8;
                }
            }
        }
        return 1;
    }

    private static float distanceHeuristic(int x1, int y1, int x2, int y2){
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private static boolean validNode(Tile tile, Tile other){
        Block block = control.input.block;
        if(block != null && block.canReplace(other.block())){
            return true;
        }else{
            return other.block().alwaysReplace;
        }
    }

    private static boolean astar(int startX, int startY, int endX, int endY){
        Tile start = world.tile(startX, startY);
        Tile end = world.tile(endX, endY);
        if(start == end || start == null || end == null) return false;

        costs.clear();
        closed.clear();
        parents.clear();

        int nodeLimit = 1000;
        int totalNodes = 0;

        PriorityQueue<Tile> queue = new PriorityQueue<>(10, (a, b) -> Float.compare(costs.get(a.pos(), 0f) + distanceHeuristic(a.x, a.y, end.x, end.y), costs.get(b.pos(), 0f) + distanceHeuristic(b.x, b.y, end.x, end.y)));
        queue.add(start);
        boolean found = false;
        while(!queue.isEmpty() && totalNodes++ < nodeLimit){
            Tile next = queue.poll();
            float baseCost = costs.get(next.pos(), 0f);
            if(next == end){
                found = true;
                break;
            }
            closed.add(Pos.get(next.x, next.y));
            for(Point2 point : Geometry.d4){
                int newx = next.x + point.x, newy = next.y + point.y;
                Tile child = world.tile(newx, newy);
                if(child != null && validNode(next, child)){
                    if(closed.add(child.pos())){
                        parents.put(child.pos(), next.pos());
                        costs.put(child.pos(), tileHeuristic(next, child) + baseCost);
                        queue.add(child);
                    }
                }
            }
        }

        if(!found) return false;
        int total = 0;

        points.add(Pools.obtain(Point2.class, Point2::new).set(endX, endY));

        Tile current = end;
        while(current != start && total++ < nodeLimit){
            if(current == null) return false;
            int newPos = parents.get(current.pos(), Pos.invalid);

            if(newPos == Pos.invalid) return false;

            points.add(Pools.obtain(Point2.class, Point2::new).set(Pos.x(newPos),  Pos.y(newPos)));
            current = world.tile(newPos);
        }

        points.reverse();

        return true;
    }

    /**
     * Normalizes a placement area and returns the result, ready to be used for drawing a rectangle.
     * Returned x2 and y2 will <i>always</i> be greater than x and y.
     * @param block block that will be drawn
     * @param startx starting X coordinate
     * @param starty starting Y coordinate
     * @param endx ending X coordinate
     * @param endy ending Y coordinate
     * @param snap whether to snap to a line
     * @param maxLength maximum length of area
     */
    public static NormalizeDrawResult normalizeDrawArea(Block block, int startx, int starty, int endx, int endy, boolean snap, int maxLength, float scaling){
        normalizeArea(startx, starty, endx, endy, 0, snap, maxLength);

        float offset = block.offset();

        drawResult.x = result.x * tilesize;
        drawResult.y = result.y * tilesize;
        drawResult.x2 = result.x2 * tilesize;
        drawResult.y2 = result.y2 * tilesize;

        drawResult.x -= block.size * scaling * tilesize / 2;
        drawResult.x2 += block.size * scaling * tilesize / 2;


        drawResult.y -= block.size * scaling * tilesize / 2;
        drawResult.y2 += block.size * scaling * tilesize / 2;

        drawResult.x += offset;
        drawResult.y += offset;
        drawResult.x2 += offset;
        drawResult.y2 += offset;

        return drawResult;
    }

    /**
     * Normalizes a placement area and returns the result.
     * Returned x2 and y2 will <i>always</i> be greater than x and y.
     * @param tilex starting X coordinate
     * @param tiley starting Y coordinate
     * @param endx ending X coordinate
     * @param endy ending Y coordinate
     * @param snap whether to snap to a line
     * @param rotation placement rotation
     * @param maxLength maximum length of area
     */
    public static NormalizeResult normalizeArea(int tilex, int tiley, int endx, int endy, int rotation, boolean snap, int maxLength){

        if(snap){
            if(Math.abs(tilex - endx) > Math.abs(tiley - endy)){
                endy = tiley;
            }else{
                endx = tilex;
            }
        }

        if(Math.abs(endx - tilex) > maxLength){
            endx = Mathf.sign(endx - tilex) * maxLength + tilex;
        }

        if(Math.abs(endy - tiley) > maxLength){
            endy = Mathf.sign(endy - tiley) * maxLength + tiley;
        }

        int dx = endx - tilex, dy = endy - tiley;

        if(Math.abs(dx) > Math.abs(dy)){
            if(dx >= 0){
                rotation = 0;
            }else{
                rotation = 2;
            }
        }else if(Math.abs(dx) < Math.abs(dy)){
            if(dy >= 0){
                rotation = 1;
            }else{
                rotation = 3;
            }
        }

        if(endx < tilex){
            int t = endx;
            endx = tilex;
            tilex = t;
        }
        if(endy < tiley){
            int t = endy;
            endy = tiley;
            tiley = t;
        }

        result.x2 = endx;
        result.y2 = endy;
        result.x = tilex;
        result.y = tiley;
        result.rotation = rotation;

        return result;
    }

    public static class NormalizeDrawResult{
        float x, y, x2, y2;
    }

    public static class NormalizeResult{
        public int x, y, x2, y2, rotation;

        boolean isX(){
            return Math.abs(x2 - x) > Math.abs(y2 - y);
        }

        /**
         * Returns length of greater edge of the selection.
         */
        int getLength(){
            return Math.max(x2 - x, y2 - y);
        }

        /**
         * Returns the X position of a specific index along this area as a line.
         */
        int getScaledX(int i){
            return x + (x2 - x > y2 - y ? i : 0);
        }

        /**
         * Returns the Y position of a specific index along this area as a line.
         */
        int getScaledY(int i){
            return y + (x2 - x > y2 - y ? 0 : i);
        }
    }

    public interface DistanceHeuristic{
        float cost(int x1, int y1, int x2, int y2);
    }

    public interface TileHueristic{
        float cost(Tile tile, Tile other);
    }


    // zones add begon
    /**
     *  返回结果为瓦砾坐标.
     *  将放置区域规范化并返回结果, 准备用于绘制矩形. 返回x2和y2将<i>总是</i>大于x和y.<p/>
     * Normalizes a placement area and returns the result, ready to be used for drawing a rectangle.
     * Returned x2 and y2 will <i>always</i> be greater than x and y.
     * @param block block that will be drawn
     * @param startx starting X coordinate
     * @param starty starting Y coordinate
     * @param endx ending X coordinate
     * @param endy ending Y coordinate
     * @param snap whether to snap to a line
     * @param maxLength maximum length of area
     */
    public static NormalizeDrawResult normalizeDrawAreaIso(Block block, int startx, int starty, int endx, int endy, boolean snap, int maxLength, float scaling){
        normalizeArea(startx, starty, endx, endy, 0, snap, maxLength);

        float offset = ((block.size + 1) % 2);

        float dx = result.x;
        float dy = result.y;
        float dx2 = result.x2;
        float dy2 = result.y2;

        dx -= block.size * scaling * 0.5f;  // 2/1 Tile
        dx2 += block.size * scaling * 0.5f;
        dy -= block.size * scaling * 0.5f;
        dy2 += block.size * scaling * 0.5f;

        dx += offset;
        dy += offset;
        dx2 += offset;
        dy2 += offset;

//        Vec2 temp = Vec2.TEMP;
//        ISOUtils.tileToWorldCoords(dx, dy, temp);
        drawResult.x = dx;
        drawResult.y = dy;
//
//        ISOUtils.tileToWorldCoords(dx2, dy2, temp);
        drawResult.x2 = dx2;
        drawResult.y2 = dy2;

        return drawResult;
    }
    // zones add end
}
