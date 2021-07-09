package mindustry.core;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.ArcAnnotate.*;
import arc.util.*;
import arc.util.reflect.ClassReflection;
import arc.util.reflect.ReflectionException;
import arc.z.util.ISOUtils;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;
import mindustry.game.*;
import mindustry.game.Teams.*;
import mindustry.io.*;
import mindustry.maps.*;
import mindustry.maps.filters.*;
import mindustry.maps.filters.GenerateFilter.*;
import mindustry.maps.generators.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

import static mindustry.Vars.*;
import static z.debug.ZDebug.disable_mindustryEndCheck;
import static z.debug.ZDebug.disable_randomOre;
import static z.debug.ZDebug.enable_isoInput;
import static z.debug.ZDebug.enable_scriptLoader;

/**
 *  地图场景
 * */
public class World{
    public final Context context = new Context();

    /** 当前地图描述数据*/
    private Map currentMap;
    /** 地图瓦砾数据*/
    private Tile[][] tiles;

    /** 地图构建状态*/
    private boolean generating, /** 是否为有效地图*/invalidMap;

    public World(){

    }

    /** 地图数据是否有效*/
    public boolean isInvalidMap(){
        return invalidMap;
    }

    /** 是否为Solid不可同行, 指定瓦砾*/
    public boolean solid(int x, int y){
        Tile tile = tile(x, y);

        return tile == null || tile.solid();
    }

    /** 是否为非Solid可同行, 指定瓦砾*/
    public boolean passable(int x, int y){
        Tile tile = tile(x, y);

        return tile != null && tile.passable();
    }

    /** 是否为墙体, 指定瓦砾*/
    public boolean wallSolid(int x, int y){
        Tile tile = tile(x, y);
        return tile == null || tile.block().solid;
    }

    /** 是否可同行, 指定瓦砾*/
    public boolean isAccessible(int x, int y){
        return !wallSolid(x, y - 1) || !wallSolid(x, y + 1) || !wallSolid(x - 1, y) || !wallSolid(x + 1, y);
    }

    /** 当前地图描述数据*/
    public Map getMap(){
        return currentMap;
    }

    /** 设置当前地图描述数据*/
    public void setMap(Map map){
        this.currentMap = map;
    }

    /** 地图宽度, 瓦砾*/
    public int width(){
        return tiles == null ? 0 : tiles.length;
    }

    /** 地图高度, 瓦砾*/
    public int height(){
        return tiles == null ? 0 : tiles[0].length;
    }

    /** 地图宽度, 绘制像素(size 8).*/
    public int unitWidth(){
        return width()*tilesize;
    }

    /** 地图高度, 绘制像素(size 8).*/
    public int unitHeight(){
        return height()*tilesize;
    }

    /** 指定索引瓦砾*/
    public @Nullable
    Tile tile(int pos){
        return tiles == null ? null : tile(Pos.x(pos), Pos.y(pos));
    }

    /** 索引位置瓦砾*/
    public @Nullable Tile tile(int x, int y){
        if(tiles == null){
            return null;
        }
        if(!Structs.inBounds(x, y, tiles)) return null;
        return tiles[x][y];
    }

    /** 索引位置瓦砾link*/
    public @Nullable Tile ltile(int x, int y){
        Tile tile = tile(x, y);
        if(tile == null) return null;
        return tile.block().linked(tile);
    }

    /** 索引位置瓦砾, 无任何检测. */
    public Tile rawTile(int x, int y){
        return tiles[x][y];
    }

    /** 绘制位置瓦砾(size 8)*/
    public @Nullable Tile tileWorld(float x, float y){
        if (enable_isoInput) {
            Vec2 pos = ISOUtils.worldToTileCoords(x, y, Vec2.TILE_ISO);
            return tile(Math.round(pos.x), Math.round(pos.y));
        }
        return tile(Math.round(x / tilesize), Math.round(y / tilesize));
    }

    /** 绘制位置瓦砾Link(size 8)*/
    public @Nullable Tile ltileWorld(float x, float y){
        if (enable_isoInput) {
            Vec2 pos = ISOUtils.worldToTileCoords(x, y, Vec2.TILE_ISO);
            return ltile(Math.round(pos.x), Math.round(pos.y));
        }
        return ltile(Math.round(x / tilesize), Math.round(y / tilesize));
    }

    /** 绘制转瓦砾坐标(size 8)*/
    public int toTile(float coord){
        return Math.round(coord / tilesize);
    }

    /** 瓦砾池*/
    public Tile[][] getTiles(){
        return tiles;
    }

    /** 清除地图瓦砾实体*/
    private void clearTileEntities(){
        for(int x = 0; x < tiles.length; x++){
            for(int y = 0; y < tiles[0].length; y++){
                if(tiles[x][y] != null && tiles[x][y].entity != null){
                    tiles[x][y].entity.remove();
                }
            }
        }
    }

    /** 调整地图尺寸.<p/>
     * Resizes the tile array to the specified size and returns the resulting tile array.
     * Only use for loading saves!
     */
    public Tile[][] createTiles(int width, int height){
        if(tiles != null){
            clearTileEntities();

            if(tiles.length != width || tiles[0].length != height){
                tiles = new Tile[width][height];
            }
        }else{
            tiles = new Tile[width][height];
        }

        return tiles;
    }

    /**
     *  表示地图构建开始, 在调用endMapLoad()之前TileChangeEvents将不会执行.<p/>
     * Call to signify the beginning of map loading.
     * TileChangeEvents will not be fired until endMapLoad().
     */
    public void beginMapLoad(){
        generating = true;
    }

    /**
     *  调用表示地图构建完毕.<p/>
     * Call to signify the end of map loading. Updates tile occlusions and sets up physics for the world.
     * A WorldLoadEvent will be fire.
     */
    public void endMapLoad(){
        prepareTiles(tiles);

        for(int x = 0; x < tiles.length; x++){
            for(int y = 0; y < tiles[0].length; y++){
                Tile tile = tiles[x][y];
                tile.updateOcclusion();

                if(tile.entity != null){
                    tile.entity.updateProximity();
                }
            }
        }

        if(!headless){
            addDarkness(tiles);
        }

        entities.all().each(group -> group.resize(-finalWorldBounds, -finalWorldBounds, tiles.length * tilesize + finalWorldBounds * 2, tiles[0].length * tilesize + finalWorldBounds * 2));

        generating = false;
        // zones add begon
        Events.fire(new GroundSystemInitEvent());
        // zones add end
        Events.fire(new WorldLoadEvent());
    }

    /** 设置地图构建状态*/
    public void setGenerating(boolean gen){
        this.generating = gen;
    }

    /** 地图构建状态*/
    public boolean isGenerating(){
        return generating;
    }

    /***/
    public boolean isZone(){
        return getZone() != null;
    }

    /***/
    public Zone getZone(){
        return state.rules.zone;
    }

    /** 加载地图构建器*/
    public void loadGenerator(Generator generator){
        beginMapLoad();

        createTiles(generator.width, generator.height);
        generator.generate(tiles);

        endMapLoad();
    }

    /** 加载指定地图*/
    public void loadMap(Map map){
        loadMap(map, new Rules());
    }

    /** 加载地图*/
    public void loadMap(Map map, Rules checkRules){
        try{
            SaveIO.load(map.file, new FilterContext(map));
        }catch(Throwable e){
            Log.err(e);
            if(!headless){
                ui.showErrorMessage("$map.invalid");
                Core.app.post(() -> state.set(State.menu));
                invalidMap = true;
            }
            generating = false;
            return;
        }

        this.currentMap = map;

        invalidMap = false;
        if ( disable_mindustryEndCheck) {
            if (enable_scriptLoader) { // 加载地图脚本文件
                if (map.tags.get("scripts") != null) {
                    try {
                        ClassReflection.newInstance(ClassReflection.forName(map.tags.get("scripts")));
                    } catch (ReflectionException e) {
                        Log.warn("World: " + map.name() + ": " + " loading scripts failded.");
                    }
                }
            }
        } else {
            if(!headless){
                if(state.teams.playerCores().size == 0 && !checkRules.pvp){
                    ui.showErrorMessage("$map.nospawn");
                    invalidMap = true;
                }else if(checkRules.pvp){ //pvp maps need two cores to be valid
                    if(state.teams.getActive().count(TeamData::hasCore) < 2){
                        invalidMap = true;
                        ui.showErrorMessage("$map.nospawn.pvp");
                    }
                }else if(checkRules.attackMode){ //attack maps need two cores to be valid
                    invalidMap = state.teams.get(state.rules.waveTeam).noCores();
                    if(invalidMap){
                        ui.showErrorMessage("$map.nospawn.attack");
                    }
                }
            }else{
                invalidMap = !state.teams.getActive().contains(TeamData::hasCore);

                if(invalidMap){
                    throw new MapException(map, "Map has no cores!");
                }
            }

            if(invalidMap) Core.app.post(() -> state.set(State.menu));
        }
    }

    /** 瓦砾改变事件通知*/
    public void notifyChanged(Tile tile){
        if(!generating){
            Core.app.post(() -> Events.fire(new TileChangeEvent(tile)));
        }
    }

    /** 路径消耗*/
    public void raycastEachWorld(float x0, float y0, float x1, float y1, Raycaster cons){
        raycastEach(toTile(x0), toTile(y0), toTile(x1), toTile(y1), cons);
    }

    /** 路径消耗*/
    public void raycastEach(int x0f, int y0f, int x1, int y1, Raycaster cons){
        int x0 = x0f;
        int y0 = y0f;
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx - dy;
        int e2;
        while(true){

            if(cons.accept(x0, y0)) break;
            if(x0 == x1 && y0 == y1) break;

            e2 = 2 * err;
            if(e2 > -dy){
                err = err - dy;
                x0 = x0 + sx;
            }

            if(e2 < dx){
                err = err + dx;
                y0 = y0 + sy;
            }
        }
    }

    /** 添加地图黑雾*/
    public void addDarkness(Tile[][] tiles){
        byte[][] dark = new byte[tiles.length][tiles[0].length];
        byte[][] writeBuffer = new byte[tiles.length][tiles[0].length];

        byte darkIterations = 4;
        for(int x = 0; x < tiles.length; x++){
            for(int y = 0; y < tiles[0].length; y++){
                Tile tile = tiles[x][y];
                if(tile.isDarkened()){
                    dark[x][y] = darkIterations;
                }
            }
        }

        for(int i = 0; i < darkIterations; i++){
            for(int x = 0; x < tiles.length; x++){
                for(int y = 0; y < tiles[0].length; y++){
                    boolean min = false;
                    for(Point2 point : Geometry.d4){
                        int newX = x + point.x, newY = y + point.y;
                        if(Structs.inBounds(newX, newY, tiles) && dark[newX][newY] < dark[x][y]){
                            min = true;
                            break;
                        }
                    }
                    writeBuffer[x][y] = (byte)Math.max(0, dark[x][y] - Mathf.num(min));
                }
            }

            for(int x = 0; x < tiles.length; x++){
                System.arraycopy(writeBuffer[x], 0, dark[x], 0, tiles[0].length);
            }
        }

        for(int x = 0; x < tiles.length; x++){
            for(int y = 0; y < tiles[0].length; y++){
                Tile tile = tiles[x][y];
                if(tile.isDarkened()){
                    tiles[x][y].rotation(dark[x][y]);
                }
                if(dark[x][y] == 4){
                    boolean full = true;
                    for(Point2 p : Geometry.d4){
                        int px = p.x + x, py = p.y + y;
                        if(Structs.inBounds(px, py, tiles) && !(tiles[px][py].isDarkened() && dark[px][py] == 4)){
                            full = false;
                            break;
                        }
                    }

                    if(full) tiles[x][y].rotation(5);
                }
            }
        }
    }

    /** 瓦砾准备.<p/>
     * 'Prepares' a tile array by:<br>
     * - setting up multiblocks<br>
     * - updating occlusion<br>
     * Usually used before placing structures on a tile array.
     */
    public void prepareTiles(Tile[][] tiles){

        //find multiblocks
        IntArray multiblocks = new IntArray();

        for(int x = 0; x < tiles.length; x++){
            for(int y = 0; y < tiles[0].length; y++){
                Tile tile = tiles[x][y];

                if(tile.block().isMultiblock()){
                    multiblocks.add(tile.pos());
                }
            }
        }

        //place multiblocks now
        for(int i = 0; i < multiblocks.size; i++){
            int pos = multiblocks.get(i);

            int x = Pos.x(pos);
            int y = Pos.y(pos);

            Block result = tiles[x][y].block();
            Team team = tiles[x][y].getTeam();

            int offsetx = -(result.size - 1) / 2;
            int offsety = -(result.size - 1) / 2;

            for(int dx = 0; dx < result.size; dx++){
                for(int dy = 0; dy < result.size; dy++){
                    int worldx = dx + offsetx + x;
                    int worldy = dy + offsety + y;
                    if(!(worldx == x && worldy == y)){
                        Tile toplace = world.tile(worldx, worldy);
                        if(toplace != null){
                            toplace.setBlock(BlockPart.get(dx + offsetx, dy + offsety), team);
                        }
                    }
                }
            }
        }
    }

    /**
     *  路径消耗
     * */
    public interface Raycaster{
        boolean accept(int x, int y);
    }

    /**
     *  地图内容
     * */
    private class Context implements WorldContext{
        @Override
        public Tile tile(int x, int y){
            return tiles[x][y];
        }

        @Override
        public void resize(int width, int height){
            createTiles(width, height);
        }

        @Override
        public Tile create(int x, int y, int floorID, int overlayID, int wallID){
            return (tiles[x][y] = new Tile(x, y, floorID, overlayID, wallID));
        }

        @Override
        public boolean isGenerating(){
            return World.this.isGenerating();
        }

        @Override
        public void begin(){
            beginMapLoad();
        }

        @Override
        public void end(){
            endMapLoad();
        }
    }

    /**
     *  过滤地图内容.<p/>
     * World context that applies filters after generation end.
     * */
    private class FilterContext extends Context{
        final Map map;

        FilterContext(Map map){
            this.map = map;
        }

        @Override
        public void end(){
            Array<GenerateFilter> filters = map.filters();
            if(!filters.isEmpty() && !disable_randomOre){
                //input for filter queries
                GenerateInput input = new GenerateInput();

                for(GenerateFilter filter : filters){
                    input.begin(filter, width(), height(), (x, y) -> tiles[x][y]);

                    //actually apply the filter
                    for(int x = 0; x < width(); x++){
                        for(int y = 0; y < height(); y++){
                            Tile tile = rawTile(x, y);
                            input.apply(x, y, tile.floor(), tile.block(), tile.overlay());
                            filter.apply(input);

                            tile.setFloor((Floor)input.floor);
                            tile.setOverlay(input.ore);

                            if(!tile.block().synthetic() && !input.block.synthetic()){
                                tile.setBlock(input.block);
                            }
                        }
                    }
                }
            }

            super.end();
        }
    }


    // zones add begon

    /** 绘制转瓦砾坐标(size 8)*/
    public int toTileX(float wx, float wy){
        return Math.round(ISOUtils.worldToTileX(wx, wy));
    }

    /** 绘制转瓦砾坐标(size 8)*/
    public int toTileY(float wx, float wy){
        return Math.round(ISOUtils.worldToTileY(wx, wy));
    }

    /** 索引位置瓦砾*/
    public @Nullable Tile tile(float x, float y){
        return tile(Math.round(x), Math.round(y));
    }

    // zones add end
}
