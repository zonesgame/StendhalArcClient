package mindustry.graphics;

import java.util.Arrays;

import arc.Core;
import arc.Events;
import arc.graphics.Camera;
import arc.graphics.Gl;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.MultiCacheBatch;
import arc.graphics.g2d.SpriteBatch;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.IntArray;
import arc.struct.IntSet;
import arc.struct.IntSet.IntSetIterator;
import arc.struct.ObjectSet;
import arc.util.ArcRuntimeException;
import arc.util.Disposable;
import arc.util.Log;
import arc.util.Structs;
import arc.util.Time;
import arc.util.Tmp;
import arc.z.util.ISOUtils;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.world.Pos;
import mindustry.world.Tile;
import mindustry.world.blocks.Floor;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.tileunit;
import static mindustry.Vars.world;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  地板绘制器
 * */
public class FloorRenderer implements Disposable{
    /** 缓存块尺寸*/
    private final static int chunksize = enable_isoInput ? 32 : 64;

    /** 缓存块容器*/
    private Chunk[][] cache;
    /** 缓存渲染器*/
    private MultiCacheBatch cbatch;
    /** 绘制图层单集*/
    private IntSet drawnLayerSet = new IntSet();
    /** 绘制图层*/
    private IntArray drawnLayers = new IntArray();
    /** 使用缓存图层*/
    private ObjectSet<CacheLayer> used = new ObjectSet<>();

    public FloorRenderer(){
        Events.on(WorldLoadEvent.class, event -> clearTiles());
    }

    // debug delete code begon
    // debug delete code end
    /** 绘制地板*/
    public void drawFloor(){
        if (enable_isoInput) {      // 禁用缓存调试绘制
            if (true) {     // 使用暗黑斜45绘制方法测试
                if(cache == null){
                    return;
                }

                Camera camera = Core.camera;
                Rect rect = camera.bounds(Tmp.r3);

                int col1 = (int) ISOUtils.worldToChunkY(rect.x, rect.y + rect.height);
                int col2 = (int) ISOUtils.worldToChunkY(rect.x + rect.width, rect.y);

                int row1 = (int) ISOUtils.worldToChunkX(rect.x, rect.y);
                int row2 = (int) ISOUtils.worldToChunkX(rect.x + rect.width, rect.y + rect.height);

                int layers = CacheLayer.values().length;

                drawnLayers.clear();
                drawnLayerSet.clear();

                //preliminary layer check
                for (int row = row2; row >= row1; row--) {
                    for (int col = col1; col <= col2; col++) {
                        int worldx = row;
                        int worldy = col;

                        if(!Structs.inBounds(worldx, worldy, cache))
                            continue;

                        Chunk chunk = cache[worldx][worldy];

                        //loop through all layers, and add layer index if it exists
                        for(int i = 0; i < layers; i++){
                            if(chunk.caches[i] != -1 && i != CacheLayer.walls.ordinal()){
                                drawnLayerSet.add(i);
                            }
                        }
                    }
                }

                IntSetIterator it = drawnLayerSet.iterator();
                while(it.hasNext){
                    drawnLayers.add(it.next());
                }

                drawnLayers.sort();

                Draw.flush();
                beginDraw();

                for(int i = 0; i < drawnLayers.size; i++){
                    CacheLayer layer = CacheLayer.values()[drawnLayers.get(i)];

                    drawLayer(layer);
                }

                endDraw();
                return;
            }

            Draw.shader();
            Draw.reset();

            Camera camera = Core.camera;
            IntArray cache = new IntArray();

            // 偏移像素单位. pixel offset of sub-tile in world-space
            float spx = ISOUtils.TILE_WIDTH * -0.5f;
            float spy = ISOUtils.TILE_HEIGHT *  -0;

            Rect rect = camera.bounds(Tmp.r3);
            rect.set(rect.x - spx, rect.y - spy, rect.width + spx * 2, rect.height + spy * 2);
            // 减去Draw绘制偏移量
            rect.setPosition(rect.x - ISOUtils.TILE_WIDTH50, rect.y - ISOUtils.TILE_HEIGHT50);
            Vec2 pos = ISOUtils.worldToTileCoords(rect.x, rect.y);

            int renderMinX = (int) pos.x;
            int renderMinY = (int) pos.y;
            boolean subY = (pos.x + pos.y) - ((int)pos.x + (int)pos.y) < tileunit;

            if (pos.y > pos.x) {
                if (subY) {
                    subY = false;
                    renderMinX--;
                } else {
                    renderMinY++;
                }
            }

            int renderWidth = Mathf.ceil(rect.width / ISOUtils.TILE_WIDTH) + 2;     // default 2
            int renderHeight = Mathf.ceil(rect.height / ISOUtils.TILE_HEIGHT50) + 3;    // default 3
            {   // test code
//                renderWidth--;
//                renderHeight--;
            }

            for (int y = 0; y < renderHeight; y++) {
                int stx;
                int sty;
                stx = subY ? renderMinX : renderMinX++;
                sty = subY ? renderMinY-- : renderMinY;
                subY = !subY;

                for (int x = 0; x < renderWidth; x++) {
                    int tx = stx + x;
                    int ty = sty + x;
                    Tile tile = world.tile(tx, ty);
                    if (tile != null) {
                        int value = Pos.get(tile.x, tile.y);
                        if (cache.contains(value))
                           throw new ArcRuntimeException("Contaions Exceptons...");
                        cache.add(value);
                    }
                }
            }
//            cache.sort();

            // 绘制测试
            for (int value : cache.items) {
                Tile tile = world.tile(value);
                if (tile.floor() != null)
                    tile.floor().draw(tile);
                if (tile.block() != null)
                    tile.block().draw(tile);
//                if (tile.overlay() != null)
//                    tile.overlay().draw(tile);
            }


            return;
        }

        if(cache == null){
            return;
        }

        Camera camera = Core.camera;

        int crangex = (int)(camera.width / (chunksize * tilesize)) + 1;
        int crangey = (int)(camera.height / (chunksize * tilesize)) + 1;

        int camx = (int)(camera.position.x / (chunksize * tilesize));
        int camy = (int)(camera.position.y / (chunksize * tilesize));

        int layers = CacheLayer.values().length;

        drawnLayers.clear();
        drawnLayerSet.clear();

        //preliminary layer check
        for(int x = -crangex; x <= crangex; x++){
            for(int y = -crangey; y <= crangey; y++){
                int worldx = camx + x;
                int worldy = camy + y;

                if(!Structs.inBounds(worldx, worldy, cache))
                    continue;

                Chunk chunk = cache[worldx][worldy];

                //loop through all layers, and add layer index if it exists
                for(int i = 0; i < layers; i++){
                    if(chunk.caches[i] != -1 && i != CacheLayer.walls.ordinal()){
                        drawnLayerSet.add(i);
                    }
                }
            }
        }

        IntSetIterator it = drawnLayerSet.iterator();
        while(it.hasNext){
            drawnLayers.add(it.next());
        }

        drawnLayers.sort();

        Draw.flush();
        beginDraw();

        for(int i = 0; i < drawnLayers.size; i++){
            CacheLayer layer = CacheLayer.values()[drawnLayers.get(i)];

            drawLayer(layer);
        }

        endDraw();
    }

    public void beginc(){
        cbatch.beginDraw();
    }

    public void endc(){
        cbatch.endDraw();
    }

    public void beginDraw(){
        if(cache == null){
            return;
        }

        cbatch.setProjection(Core.camera.projection());
        cbatch.beginDraw();

        Gl.enable(Gl.blend);
    }

    public void endDraw(){
        if(cache == null){
            return;
        }

        cbatch.endDraw();
    }

    public void drawLayer(CacheLayer layer){
        if (enable_isoInput) {
            if(cache == null){
                return;
            }

            if (true) {
                Camera camera = Core.camera;
                Rect rect = camera.bounds(Tmp.r3);

                int col1 = (int) ISOUtils.worldToChunkY(rect.x, rect.y + rect.height);
                int col2 = (int) ISOUtils.worldToChunkY(rect.x + rect.width, rect.y);

                int row1 = (int) ISOUtils.worldToChunkX(rect.x, rect.y);
                int row2 = (int) ISOUtils.worldToChunkX(rect.x + rect.width, rect.y + rect.height);

                layer.begin();

                for (int row = row2; row >= row1; row--) {
                    for (int col = col1; col <= col2; col++) {
                        int worldx = row;
                        int worldy = col;

                        if(!Structs.inBounds(worldx, worldy, cache)){
                            continue;
                        }

                        Chunk chunk = cache[worldx][worldy];
                        if(chunk.caches[layer.ordinal()] == -1) continue;
                        cbatch.drawCache(chunk.caches[layer.ordinal()]);
                    }
                }

                layer.end();
                return;
            }

            Camera camera = Core.camera;

            // 偏移像素单位. pixel offset of sub-tile in world-space
            float spx = ISOUtils.CHUNK_WIDTH * 0;
            float spy = ISOUtils.CHUNK_HEIGHT *  0;

            Rect rect = camera.bounds(Tmp.r3);
            rect.set(rect.x - spx, rect.y - spy, rect.width + spx * 2, rect.height + spy * 2);
            // 减去Draw绘制偏移量
            rect.setPosition(rect.x - ISOUtils.TILE_WIDTH50, rect.y - ISOUtils.TILE_HEIGHT50);
            Vec2 pos = ISOUtils.worldToChunkCoords(rect.x, rect.y);
            System.out.println(pos.x + "            " + pos.y);

            int renderMinX = (int) pos.x;
            int renderMinY = (int) pos.y;
            boolean subY = (pos.x + pos.y) - ((int)pos.x + (int)pos.y) < tileunit;
            if (pos.y > pos.x) {
                if (subY) {
                    subY = false;
                    renderMinX--;
                } else {
                    renderMinY++;
                }
            }
            int renderWidth = Mathf.ceil(rect.width / ISOUtils.CHUNK_WIDTH) + 6;     // default 2
            int renderHeight = Mathf.ceil(rect.height / ISOUtils.CHUNK_HEIGHT50) + 6;    // default 3

            layer.begin();

            //preliminary layer check
            for (int y = 0; y < renderHeight; y++) {
                int stx;
                int sty;
                stx = subY ? renderMinX : renderMinX++;
                sty = subY ? renderMinY-- : renderMinY;
                subY = !subY;

                for (int x = 0; x < renderWidth; x++) {
                    int worldx = stx + x;
                    int worldy = sty + x;

                    if(!Structs.inBounds(worldx, worldy, cache)){
                        continue;
                    }

                    Chunk chunk = cache[worldx][worldy];
                    if(chunk.caches[layer.ordinal()] == -1) continue;
                    cbatch.drawCache(chunk.caches[layer.ordinal()]);
                }
            }

            layer.end();
            return;
        }

        if(cache == null){
            return;
        }

        Camera camera = Core.camera;

        int crangex = (int)(camera.width / (chunksize * tilesize)) + 1;
        int crangey = (int)(camera.height / (chunksize * tilesize)) + 1;

        layer.begin();

        for(int x = -crangex; x <= crangex; x++){
            for(int y = -crangey; y <= crangey; y++){
                int worldx = (int)(camera.position.x / (chunksize * tilesize)) + x;
                int worldy = (int)(camera.position.y / (chunksize * tilesize)) + y;

                if(!Structs.inBounds(worldx, worldy, cache)){
                    continue;
                }

                Chunk chunk = cache[worldx][worldy];
                if(chunk.caches[layer.ordinal()] == -1) continue;
                cbatch.drawCache(chunk.caches[layer.ordinal()]);
            }
        }

        layer.end();
    }

    /** 设置缓存块数据*/
    private void cacheChunk(int cx, int cy){
        used.clear();
        Chunk chunk = cache[cx][cy];

        for(int tilex = cx * chunksize; tilex < (cx + 1) * chunksize; tilex++){
            for(int tiley = cy * chunksize; tiley < (cy + 1) * chunksize; tiley++){
                Tile tile = world.tile(tilex, tiley);

                if(tile != null){
                    if(tile.block().cacheLayer != CacheLayer.normal){
                        used.add(tile.block().cacheLayer);
                    }else{
                        used.add(tile.floor().cacheLayer);
                    }
                }
            }
        }

        for(CacheLayer layer : used){
            cacheChunkLayer(cx, cy, chunk, layer);
        }
    }

    /** 设置缓存块图层数据*/
    private void cacheChunkLayer(int cx, int cy, Chunk chunk, CacheLayer layer){
        SpriteBatch current = Core.batch;
        Core.batch = cbatch;

        cbatch.beginCache();

        for(int tilex = cx * chunksize; tilex < (cx + 1) * chunksize; tilex++){
            for(int tiley = cy * chunksize; tiley < (cy + 1) * chunksize; tiley++){
                Tile tile = world.tile(enable_isoInput ? (chunksize - 1 - (tilex - (cx * chunksize)) + cx * chunksize) : tilex, tiley);
                Floor floor;

                if(tile == null){
                    continue;
                }else{
                    floor = tile.floor();
                }

                if(tile.block().cacheLayer == layer && layer == CacheLayer.walls && !(tile.isDarkened() && tile.rotation() >= 5)){
                    tile.block().draw(tile);
                }else if(floor.cacheLayer == layer && (world.isAccessible(tile.x, tile.y) || tile.block().cacheLayer != CacheLayer.walls || !tile.block().fillsTile)){
                    floor.draw(tile);
                }else if(floor.cacheLayer.ordinal() < layer.ordinal() && layer != CacheLayer.walls){
                    floor.drawNonLayer(tile);
                }
            }
        }
        Core.batch = current;
        chunk.caches[layer.ordinal()] = cbatch.endCache();
    }

    /** 创建缓存纹理*/
    public void clearTiles(){
        if(cbatch != null) cbatch.dispose();

        int chunksx = Mathf.ceil((float)(world.width()) / chunksize),
        chunksy = Mathf.ceil((float)(world.height()) / chunksize);
        cache = new Chunk[chunksx][chunksy];
        cbatch = new MultiCacheBatch(chunksize * chunksize * 5);

        Time.mark();

        for(int x = 0; x < chunksx; x++){
            for(int y = 0; y < chunksy; y++){
                cache[x][y] = new Chunk();
                Arrays.fill(cache[x][y].caches, -1);    // 填充缓存块

                cacheChunk(x, y);
            }
        }

        Log.info("Time to cache: {0}", Time.elapsed());
    }

    @Override
    public void dispose(){
        if(cbatch != null){
            cbatch.dispose();
            cbatch = null;
        }
    }

    /**
     *  缓存块
     * */
    private class Chunk{
        int[] caches = new int[CacheLayer.values().length];
    }


    // zones add begon
//    public void drawBackgroundImage() {
//        TextureRegion[] bgImage = Assets.bgImage;
//        float scale = Draw.scl;
//        float height = 250;
//        for (int i = 10; i >= 0; i--) {
//            float startx = 0;
//            for (int j = 0; j < 11; j++) {
//                int index = i * 11 + j;
//                float dx = startx * scale;
//                float dy = i * height * scale;
//                Draw.rectGdx(bgImage[index], dx, dy);
//                startx += bgImage[index].getWidth();
//            }
//        }
//    }
    // zones add end
}
