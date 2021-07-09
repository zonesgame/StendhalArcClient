package mindustry.ai;

import arc.Events;
import arc.func.Cons2;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.math.geom.Position;
import arc.struct.Array;
import arc.struct.GridBits;
import arc.struct.IntArray;
import arc.struct.IntQueue;
import arc.util.ArcAnnotate.Nullable;
import arc.util.Structs;
import arc.util.TaskQueue;
import arc.util.Time;
import arc.util.async.Threads;
import mindustry.annotations.Annotations.Struct;
import mindustry.game.EventType.ResetEvent;
import mindustry.game.EventType.TileChangeEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.game.Team;
import mindustry.gen.PathTile;
import mindustry.world.Pos;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import z.ai.astar.FlatTiledAStar;
import z.ai.astar.FlatTiledNode;
import z.ai.astar.TiledSmoothableGraphPath;
import z.utils.FinalCons;

import static mindustry.Vars.indexer;
import static mindustry.Vars.net;
import static mindustry.Vars.spawner;
import static mindustry.Vars.state;
import static mindustry.Vars.world;
import static z.debug.ZDebug.enable_allPassable;

public class Pathfinder implements Runnable{
    private static final long maxUpdate = Time.millisToNanos(4);
    private static final int updateFPS = 60;
    private static final int updateInterval = 1000 / updateFPS;
    private static final int impassable = -1;

    /** tile data, see PathTileStruct */
    private int[][] tiles;
    /** unordered array of path data for iteration only. DO NOT iterate ot access this in the main thread.*/
    private Array<PathData> list = new Array<>();
    /** Maps teams + flags to a valid path to get to that flag for that team. */
    private PathData[][] pathMap = new PathData[Team.all().length][PathTarget.all.length];
    /** Grid map of created path data that should not be queued again. */
    private GridBits created = new GridBits(Team.all().length, PathTarget.all.length);
    /** handles task scheduling on the update thread. */
    private TaskQueue queue = new TaskQueue();
    /** current pathfinding thread */
    private @Nullable Thread thread;

    public Pathfinder(){
        Events.on(WorldLoadEvent.class, event -> {
            stop();

            //reset and update internal tile array
            tiles = new int[world.width()][world.height()];
            pathMap = new PathData[Team.all().length][PathTarget.all.length];
            created = new GridBits(Team.all().length, PathTarget.all.length);
            list = new Array<>();
            // zones add begon
            for (int s = 0; s < squadTarget.length; s++) {
                for (int m = 0; m < squadTarget[s].length; m++) {
                    final int _squad = s, _member = m;
                    squadTarget[s][m] = new PathTarget((team, out) -> {
                        Tile _target = squadTargetTile[team.id][_squad][_member];
                        if (_target != null) {
                            out.add(_target.pos());
                        }
                    });
                }
            }
            squadTargetPos = new int[teamCount][squadCount][memberCount];
            for (int i = 0, len = extendCreated.length; i < len; i++) {
                extendCreated[i] = new GridBits(squadCount, memberCount);
            }
            extendPathMap = new PathData[teamCount][squadCount][memberCount];

            aStar.initGraph(world.getTiles(), t -> t.block().name.startsWith("road") ? false : true); // AStar算法初始化  .name.startsWith("road")
            // zones add end

            for(int x = 0; x < world.width(); x++){
                for(int y = 0; y < world.height(); y++){
                    tiles[x][y] = packTile(world.rawTile(x, y));
                }
            }

            //特别预置可以帮助加快速度;这是可选的. special preset which may help speed things up; this is optional
            preloadPath(state.rules.waveTeam, PathTarget.enemyCores);

            start();
        });

        Events.on(ResetEvent.class, event -> stop());

        Events.on(TileChangeEvent.class, event -> updateTile(event.tile));
    }

    /** Packs a tile into its internal representation. */
    private int packTile(Tile tile){
        return PathTile.get(tile.cost, tile.getTeamID(), (byte)0, !tile.solid() && tile.floor().drownTime <= 0f);
    }

    /** Starts or restarts the pathfinding thread. */
    private void start(){
        stop();
        thread = Threads.daemon(this);
    }

    /** Stops the pathfinding thread. */
    private void stop(){
        if(thread != null){
            thread.interrupt();
            thread = null;
        }
        queue.clear();
    }

    public int debugValue(Team team, int x, int y){
        if(pathMap[team.id][PathTarget.enemyCores.ordinal()] == null) return 0;
        return pathMap[team.id][PathTarget.enemyCores.ordinal()].weights[x][y];
    }

    /** Update a tile in the internal pathfinding grid. Causes a complete pathfinding reclaculation. */
    public void updateTile(Tile tile){
        if(net.client()) return;

        int x = tile.x, y = tile.y;

        tile.getLinkedTiles(t -> {
            if(Structs.inBounds(t.x, t.y, tiles)){
                tiles[t.x][t.y] = packTile(t);
                // 更新AStar路径图 zones add begon
                aStar.updateTile(t.x, t.y, () -> tile.block().name.startsWith("road") ? false : true);   // .name.startsWith("road")
                // zones add end
            }
        });

        //can't iterate through array so use the map, which should not lead to problems
        for(PathData[] arr : pathMap){
            for(PathData path : arr){
                if(path != null){
                    synchronized(path.targets){
                        path.targets.clear();
                        path.target.getTargets(path.team, path.targets);
                    }
                }
            }
        }
        // zones add begon
        for(PathData[][] arrTeam : extendPathMap){
            for(PathData[] arrSquad : arrTeam){
                for (PathData path : arrSquad) {
                    if(path != null){
                        synchronized(path.targets){
                            path.targets.clear();
                            path.target.getTargets(path.team, path.targets);
                        }
                    }
                }
            }
        }
        // zones add end

        queue.post(() -> {
            for(PathData data : list){
                updateTargets(data, x, y);
            }
        });
    }

    /** Thread implementation. */
    @Override
    public void run(){
        while(true){
            if(net.client()) return;
            try{

                queue.run();

                //total update time no longer than maxUpdate
                for(PathData data : list){
                    updateFrontier(data, maxUpdate / list.size);
                }

                try{
                    Thread.sleep(updateInterval);
                }catch(InterruptedException e){
                    //stop looping when interrupted externally
                    return;
                }
            }catch(Throwable e){
                e.printStackTrace();
            }
        }
    }

    /** 获取目标点下一个有效瓦砾,Only主线程调用.<p/>Gets next tile to travel to. Main thread only. */
    public Tile getTargetTile(Tile tile, Team team, PathTarget target){
        if(tile == null) return null;

        PathData data = pathMap[team.id][target.ordinal()];

        if(data == null){
            //if this combination is not found, create it on request
            if(!created.get(team.id, target.ordinal())){
                created.set(team.id, target.ordinal());
                //grab targets since this is run on main thread
                IntArray targets = target.getTargets(team, new IntArray());
                queue.post(() -> createPath(team, target, targets));
            }
            return tile;
        }

        // zones add begon
//        if (target == PathTarget.moveIndexer) {
//            if (indexer.moveIndexer != null && indexer.moveIndexer.pos() != movePos) {
//                movePos = indexer.moveIndexer.pos();
//
//                if(net.client()) return tile;
//                Tile targetTile = indexer.moveIndexer;
//                if (targetTile == null) return tile;
//
//                int x = targetTile.x, y = targetTile.y;
//
//                targetTile.getLinkedTiles(t -> {
//                    if(Structs.inBounds(t.x, t.y, tiles)){
//                        tiles[t.x][t.y] = packTile(t);
//                    }
//                });
//
//                //can't iterate through array so use the map, which should not lead to problems
//                if(data != null){
//                    synchronized(data.targets){
//                        data.targets.clear();
//                        data.target.getTargets(data.team, data.targets);
//                    }
//
//                    queue.post(() -> {
//                        updateTargets(data, x, y);
//                    });
//                }
//
//                return tile;
//            }
//        }
        // zones add end

        int[][] values = data.weights;
        int value = values[tile.x][tile.y];

        Tile current = null;
        int tl = 0;
        for(Point2 point : Geometry.d8){
            int dx = tile.x + point.x, dy = tile.y + point.y;

            Tile other = world.tile(dx, dy);
            if(other == null) continue;

            if(values[dx][dy] < value && (current == null || values[dx][dy] < tl) && !other.solid() && other.floor().drownTime <= 0 &&
            !(point.x != 0 && point.y != 0 && (world.solid(tile.x + point.x, tile.y) || world.solid(tile.x, tile.y + point.y)))){ //diagonal corner trap
                current = other;
                tl = values[dx][dy];
            }
        }

        if(current == null || tl == impassable) return tile;

        return current;
    }

    /** @return whether a tile can be passed through by this team. Pathfinding thread only.*/
    private boolean passable(int x, int y, Team team){
        if (enable_allPassable)
            return true;
        int tile = tiles[x][y];
        return PathTile.passable(tile) || (PathTile.team(tile) != team.id && PathTile.team(tile) != (int)Team.derelict.id);
    }

    /** 清除边界,增加搜索,并设置所有流源.这只发生在活跃的团队中.<p/>
     * Clears the frontier, increments the search and sets up all flow sources.
     * This only occurs for active teams.
     */
    private void updateTargets(PathData path, int x, int y){
        if(!Structs.inBounds(x, y, path.weights)) return;

        if(path.weights[x][y] == 0){
            //this was a previous target
            path.frontier.clear();
        }else if(!path.frontier.isEmpty()){
            //skip if this path is processing
            return;
        }

        //assign impassability to the tile
        if(!passable(x, y, path.team)){
            path.weights[x][y] = impassable;
        }

        //increment search, clear frontier
        path.search++;
        path.frontier.clear();

        synchronized(path.targets){
            //add targets
            for(int i = 0; i < path.targets.size; i++){
                int pos = path.targets.get(i);
                int tx = Pos.x(pos), ty = Pos.y(pos);

                path.weights[tx][ty] = 0;
                path.searches[tx][ty] = (short)path.search;
                path.frontier.addFirst(pos);
            }
        }
    }

    private void preloadPath(Team team, PathTarget target){
        updateFrontier(createPath(team, target, target.getTargets(team, new IntArray())), -1);
    }

    /** Created a new flowfield that aims to get to a certain target for a certain team.
     * Pathfinding thread only. */
    private PathData createPath(Team team, PathTarget target, IntArray targets){
        PathData path = new PathData(team, target, world.width(), world.height());

        list.add(path);
        pathMap[team.id][target.ordinal()] = path;

        //grab targets from passed array
        synchronized(path.targets){
            path.targets.clear();
            path.targets.addAll(targets);
        }

        //fill with impassables by default
        for(int x = 0; x < world.width(); x++){
            for(int y = 0; y < world.height(); y++){
                path.weights[x][y] = impassable;
            }
        }

        //add targets
        for(int i = 0; i < path.targets.size; i++){
            int pos = path.targets.get(i);
            path.weights[Pos.x(pos)][Pos.y(pos)] = 0;
            path.frontier.addFirst(pos);
        }

        return path;
    }

    /** Update the frontier for a path. Pathfinding thread only. */
    private void updateFrontier(PathData path, long nsToRun){
        long start = Time.nanos();

        while(path.frontier.size > 0 && (nsToRun < 0 || Time.timeSinceNanos(start) <= nsToRun)){
            Tile tile = world.tile(path.frontier.removeLast());
            if(tile == null || path.weights == null) return; //something went horribly wrong, bail
            int cost = path.weights[tile.x][tile.y];

            //pathfinding overflowed for some reason, time to bail. the next block update will handle this, hopefully
            if(path.frontier.size >= world.width() * world.height()){
                path.frontier.clear();
                return;
            }

            if(cost != impassable){
                for(Point2 point : Geometry.d4){

                    int dx = tile.x + point.x, dy = tile.y + point.y;
                    Tile other = world.tile(dx, dy);

                    if(other != null && (path.weights[dx][dy] > cost + other.cost || path.searches[dx][dy] < path.search) && passable(dx, dy, path.team)){
                        if(other.cost < 0) throw new IllegalArgumentException("Tile cost cannot be negative! " + other);
                        path.frontier.addFirst(Pos.get(dx, dy));
                        path.weights[dx][dy] = cost + other.cost;
                        path.searches[dx][dy] = (short)path.search;
                    }
                }
            }
        }
    }

    /** A path target defines a set of targets for a path.*/
    static public class PathTarget{
        public static final PathTarget enemyCores = new PathTarget(0, (team, out) -> {
            for(Tile other : indexer.getEnemy(team, BlockFlag.core)){
                out.add(other.pos());
            }

            //spawn points are also enemies.
            if(state.rules.waves && team == state.rules.defaultTeam){
                for(Tile other : spawner.getGroundSpawns()){
                    out.add(other.pos());
                }
            }
        });
        public static final PathTarget rallyPoints = new PathTarget(1, (team, out) -> {
            for(Tile other : indexer.getAllied(team, BlockFlag.rally)){
                out.add(other.pos());
            }
        });
        // zones add begon
//        public static final PathTarget waveRallyPoints = new PathTarget(2, (team, out) -> {
////            if(state.rules.waves && team == state.rules.defaultTeam){
//                for(Tile other : spawner.getRallyPoints()){
//                    out.add(other.pos());
////                }
//            }
//        });

//        // temp code begon
        /** 点击移动的目标点*/
//        public static final PathTarget moveIndexer = new PathTarget(2, (team, out) -> {
//            if (squadGroup[team.id][0] != null) {
//                Vec2 pos = squadGroup[team.id][0].getTarget().getPosition();
//                out.add(Pos.get((int) pos.x, (int) pos.y));
//            }
//        });
        // zones add end
        ;

        private final int ordinal;
        public int ordinal() {
            return ordinal;
        }

        public static final PathTarget[] all = new PathTarget[] {enemyCores, rallyPoints /*, moveIndexer*/};

        /** 队伍目标点容器*/
        private final Cons2<Team, IntArray> targeter;

        PathTarget(Cons2<Team, IntArray> targeter){
            this(-1, targeter);
        }

        PathTarget(int ordinal, Cons2<Team, IntArray> targeter){
            this.ordinal = ordinal;
            this.targeter = targeter;
        }

        /** Get targets. This must run on the main thread.*/
        public IntArray getTargets(Team team, IntArray out){
            targeter.get(team, out);
            return out;
        }
    }

    /** Data for a specific flow field to some set of destinations. */
    class PathData{
        /** Team this path is for. */
        final Team team;
        /** Flag that is being targeted. */
        final PathTarget target;
        /** costs of getting to a specific tile */
        final int[][] weights;
        /** search IDs of each position - the highest, most recent search is prioritized and overwritten */
        final short[][] searches;
        /** search frontier, these are Pos objects */
        final IntQueue frontier = new IntQueue();
        /** all target positions; these positions have a cost of 0, and must be synchronized on! */
        final IntArray targets = new IntArray();
        /** current search ID */
        int search = 1;

        PathData(Team team, PathTarget target, int width, int height){
            this.team = team;
            this.target = target;

            this.weights = new int[width][height];
            this.searches = new short[width][height];
            this.frontier.ensureCapacity((width + height) * 3);
        }
    }

    /** Holds a copy of tile data for a specific tile position. */
    @Struct
    class PathTileStruct{
        //traversal cost
        byte cost;
        //team of block, if applicable (0 by default)
        byte team;
        //type of target; TODO remove
        byte type;
        //whether it's viable to pass this block
        boolean passable;
    }

    // zones add begon

    // astar add begon
    /** 获取AStar算法路径*/
    public void getPathList(Tile from, Tile target, TiledSmoothableGraphPath<FlatTiledNode> path) {
        path.clear();     // therad safe code    not must
        queue.post(() -> {
            aStar.getPath(from.x, from.y, target.x, target.y, path);
        });
    }

    /** 获取AStar算法路径
     *  @apiNote 线程function不要使用static temp数据
     * */
    public void getPathListNoLastNode(Tile from, Tile target, TiledSmoothableGraphPath<FlatTiledNode> path) {
        path.clear();     // therad safe code    not must
        // 方案1
//        final float widthDistance = target.block().offsetTile() + ((target.block().size + 1) / 2);     // int
//        final float heightDistance = target.block().offsetTile() + ((target.block().size + 1) / 2);
//        queue.post(() -> {
//            aStar.getPathNoLastNode(from.x, from.y, target.x, target.y, path,
//                    (curNode, endNode) -> {
//                        float xDis = Math.abs(curNode.x - target.getX());
//                        float yDis = Math.abs(curNode.y - target.getY());
//                        return (xDis <= widthDistance - tileunit && yDis <= heightDistance) || (xDis <= widthDistance && yDis <= heightDistance - tileunit);
//                    }
//            );
//        });

        // 方案4
        int offset = (target.block().size-1) / 2;
        final int startx = target.x - offset - 1;
        final int starty = target.y - offset - 1;
        final int sizex = target.block().size + 2;
        final int sizey = sizex;
        final int arrsize = sizex * sizey;
        final boolean[] arrContains = new boolean[arrsize];
        {   // 四个角设置为true
            arrContains[0] = true;      //
            arrContains[sizex - 1] = true;
            arrContains[(sizey - 1) * sizex] = true;
            arrContains[sizex * sizey - 1] = true;
        }

        queue.post(() -> {
            aStar.getPathNoLastNode(from.x, from.y, target.x, target.y, path,
                    (curNode, endNode) -> {
                        int x = curNode.x - startx;
                        int y = (curNode.y - starty);
                        int index = x + y * sizex;
                        if (x < 0 || y < 0 || x >= sizex || y >= sizey) return false;   // || index < 0 || index >= arrsize
                        else return !arrContains[index];
                    }
            );
        });
    }

    /** 获取Astar路径节点用于巡逻自动移动*/
    public FlatTiledNode getGraphNode(float x, float y) {
        return aStar.getGraphNode(Math.round(x), Math.round(y));
    }
    // astar add end

    /** 获取目标点下一个有效瓦砾,Only主线程调用.<p/>Gets next tile to travel to. Main thread only. */
    public Position getTargetMember(Tile tile, Team team, int squad, int member){
        if(tile == null) return null;

        PathData data = extendPathMap[team.id][squad][member];

        if(data == null){
            //if this combination is not found, create it on request
            if(!extendCreated[team.id].get(squad, member)){
                extendCreated[team.id].set(squad, member);
                //grab targets since this is run on main thread
                IntArray targets = squadTarget[squad][member].getTargets(team, new IntArray());
                queue.post(() -> createPathSquad(team, squad, member, squadTarget[squad][member], targets));
            }
            return tile;
        }

        // zones add begon
        // 动态计算需求目标路径,获取目标延时增加但能减少不必要主线程更新
        {   // 检测移动目标点路径是否需要更新
            Tile moveIndexer = squadTargetTile[team.id][squad][member];
            if (moveIndexer != null && moveIndexer.pos() != squadTargetPos[team.id][squad][member]) {
                squadTargetPos[team.id][squad][member] = moveIndexer.pos();

                updateTile(moveIndexer, data);
                return tile;
            }
        }
        // zones add end

        int[][] values = data.weights;
        int value = values[tile.x][tile.y];

        Tile current = null;
        int tl = 0;
        for(Point2 point : Geometry.d8){
            int dx = tile.x + point.x, dy = tile.y + point.y;

            Tile other = world.tile(dx, dy);
            if(other == null) continue;

            if(values[dx][dy] < value && (current == null || values[dx][dy] < tl) && !other.solid() && other.floor().drownTime <= 0 &&
                    !(point.x != 0 && point.y != 0 && (world.solid(tile.x + point.x, tile.y) || world.solid(tile.x, tile.y + point.y)))){ //diagonal corner trap
                current = other;
                tl = values[dx][dy];
            }
        }

        if(current == null || tl == impassable) return tile;

        return current;
    }

    /** 队伍路径使用
     * Created a new flowfield that aims to get to a certain target for a certain team.
     * Pathfinding thread only. */
    private PathData createPathSquad(Team team, int squad, int member, PathTarget target, IntArray targets){
        PathData path = new PathData(team, target, world.width(), world.height());

        list.add(path);
        extendPathMap[team.id][squad][member] = path;

        //grab targets from passed array
        synchronized(path.targets){
            path.targets.clear();
            path.targets.addAll(targets);
        }

        //fill with impassables by default
        for(int x = 0; x < world.width(); x++){
            for(int y = 0; y < world.height(); y++){
                path.weights[x][y] = impassable;
            }
        }

        //add targets
        for(int i = 0; i < path.targets.size; i++){
            int pos = path.targets.get(i);
            path.weights[Pos.x(pos)][Pos.y(pos)] = 0;
            path.frontier.addFirst(pos);
        }

        return path;
    }

    private void updateTile(Tile tile, PathData path){
        if(net.client()) return;

        int x = tile.x, y = tile.y;

        tile.getLinkedTiles(t -> {
            if(Structs.inBounds(t.x, t.y, tiles)){
                tiles[t.x][t.y] = packTile(t);
            }
        });

        //can't iterate through array so use the map, which should not lead to problems
        if(path != null){
            synchronized(path.targets){
                path.targets.clear();
                path.target.getTargets(path.team, path.targets);
            }

            queue.post(() -> {
                updateTargets(path, x, y);
            });
        }
    }


    public int teamCount = Team.all().length;
    public int squadCount = FinalCons.max_squad_count;
    public int memberCount = FinalCons.max_member_count;
//    private int movePos = -1;
    /** Grid map of created path data that should not be queued again. */
    private GridBits[] extendCreated = new GridBits[teamCount];
    /** zones扩展路径数据, 队伍编队移动算法.*/
    private PathData[][][] extendPathMap = new PathData[teamCount][squadCount][memberCount];
    /** squad path target*/
    private PathTarget[][] squadTarget = new PathTarget[squadCount][memberCount];
    /** unit move target*/
    private int[][][] squadTargetPos = new int[teamCount][squadCount][memberCount];
    /** unit move target tile*/
    public Tile[][][] squadTargetTile = new Tile[teamCount][squadCount][memberCount];

    // AStar道路路径算法
    private FlatTiledAStar aStar = new FlatTiledAStar();
    private int movePos = -1;
    // zones add end
}
