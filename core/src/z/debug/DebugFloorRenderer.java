package z.debug;

import java.util.Arrays;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.MultiCacheBatch;
import arc.graphics.g2d.SpriteBatch;
import arc.graphics.g2d.TextureRegion;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.struct.IntArray;
import arc.struct.IntSet;
import arc.struct.ObjectSet;
import arc.util.Disposable;
import arc.util.Log;
import arc.util.Time;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.graphics.CacheLayer;
import mindustry.world.Tile;
import mindustry.world.blocks.Floor;
import z.utils.ShapeRenderer;

import static mindustry.Vars.world;

/**
 *  地板绘制器
 * */
public class DebugFloorRenderer implements Disposable{
    /** 缓存块尺寸*/
    private final static int chunksize = 64;

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

    public DebugFloorRenderer(){
        Events.on(WorldLoadEvent.class, event -> clearTiles());
    }

    /** 绘制地板*/
    public void drawFloor(){
        if (true) {
            Draw.color(Color.blue);
            Lines.stroke(0.1f);
            float addx = 0;
            float addy = 0;

            for (int y = 0; y < world.height(); y++) {
                for (int x = 0; x < world.width(); x++) {
                    Tile tile = world.tile(x, y);
                    if (tile != null) {
                        ShapeRenderer.drawDiamond(x, y, 1, 1, addx, addy);    // 0.5f 确保在Tile中心点绘制
//                        drawDebugFull(x, y, 1, 1);
                    }
                }
            }

            Draw.reset();
            Draw.flush();
            return;
        }
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
        Draw.color(Color.green);
        float addx = 0;
        float addy = 0;

        for (int y = 0; y < world.height(); y++) {
            for (int x = 0; x < world.width(); x++) {
                Tile tile = world.tile(x, y);
                if(tile != null){
                    if(tile.block().cacheLayer != CacheLayer.normal){
                       ShapeRenderer.drawFillDiamond(x, y, 1, 1);
                    }
                }
            }
        }

        Draw.reset();
        Draw.flush();
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

    private void cacheChunkLayer(int cx, int cy, Chunk chunk, CacheLayer layer){
        SpriteBatch current = Core.batch;
        Core.batch = cbatch;

        cbatch.beginCache();

        for(int tilex = cx * chunksize; tilex < (cx + 1) * chunksize; tilex++){
            for(int tiley = cy * chunksize; tiley < (cy + 1) * chunksize; tiley++){
                Tile tile = world.tile(tilex, tiley);
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
                Arrays.fill(cache[x][y].caches, -1);

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

    private class Chunk{
        int[] caches = new int[CacheLayer.values().length];
    }


    // zones add begon




    /** 通过起始点和尺寸绘制菱形矩阵*/
    public static void drawIsoRect(float wx, float wy, float ww, float wh) {
    }



    TextureRegion[] bgImage = null;
    final int offset[] = {
            -67, -1396,        //  78, 60
            -44, -1397
    };
    public void drawBackgroundImage() {
        if (bgImage == null) {
            bgImage = new TextureRegion[11 * 11];
            for (int i = bgImage.length; --i >= 0; ) {
                bgImage[i] = new TextureRegion(new Texture(Core.files.internal("debug/map/50/map_" + (i+1) + ".jpg")));
                bgImage[i].getTexture().setFilter(Texture.TextureFilter.Linear);
            }
        }

        Draw.reset();
        float scale = Draw.scl;
        float starty = 0;
        for (int i = 10; i >= 0; i--) {
            float startx = 0;
            for (int j = 0; j < 11; j++) {
                int index = i * 11 + j;
                float dx = (startx + offset[0]) * scale;
                float dy = (starty + offset[1]) * scale;
                Draw.rectGdx(bgImage[index], dx, dy);
                startx += bgImage[index].getWidth();
                if (j == 10)
                    starty += bgImage[index].getHeight();
            }
        }

        if ( true) return;
        if ( Core.input.keyDown(KeyCode.Y)) {
            offset[1] += 10;
        }
        if ( Core.input.keyDown(KeyCode.U)) {
            offset[1] -= 10;
        }
        if ( Core.input.keyDown(KeyCode.I)) {
            offset[0] -= 10;
        }
        if ( Core.input.keyDown(KeyCode.O)) {
            offset[0] += 10;
        }

        if ( Core.input.keyRelease(KeyCode.F1)) {
            offset[1] += 1;
        }
        if ( Core.input.keyRelease(KeyCode.F2)) {
            offset[1] -= 1;
        }
        if ( Core.input.keyRelease(KeyCode.F3)) {
            offset[0] -= 1;
        }
        if ( Core.input.keyRelease(KeyCode.F4)) {
            offset[0] += 1;
        }
        System.out.println(offset[0] + "    " + offset[1]);
    }
    // zones add end
}
