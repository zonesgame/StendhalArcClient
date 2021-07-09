package mindustry.graphics;

import java.util.Comparator;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.Texture.TextureFilter;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.FrameBuffer;
import arc.math.Mathf;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.struct.Sort;
import arc.util.Disposable;
import arc.util.Time;
import arc.util.Tmp;
import arc.z.util.ISOUtils;
import mindustry.content.Blocks;
import mindustry.game.EventType.TileChangeEvent;
import mindustry.game.EventType.WorldLoadEvent;
import mindustry.game.Teams.BrokenBlock;
import mindustry.ui.Cicon;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.BlockPart;
import z.debug.DebugFloorRenderer;

import static arc.Core.camera;
import static mindustry.Vars.content;
import static mindustry.Vars.control;
import static mindustry.Vars.player;
import static mindustry.Vars.state;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.tileunit;
import static mindustry.Vars.world;
import static z.debug.ZDebug.debug_blockdraw;
import static z.debug.ZDebug.disable_blockSort;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  块绘制器
 * */
public class BlockRenderer implements Disposable{
    /** 初始化请求*/
    private final static int initialRequests = 32 * 32;
    /** 绘制扩展块尺寸*/
    private final static int expandr = enable_isoInput ? 2 : 9;
    /** 阴影颜色*/
    private final static Color shadowColor = new Color(0, 0, 0, 0.71f);

    /** 地板绘制器*/
    public final FloorRenderer floor = new FloorRenderer();

    /** 绘制瓦砾容器*/
    private Array<BlockRequest> requests = new Array<>(true, initialRequests, BlockRequest.class);
    /** 相机最后区域*/
    private int lastCamX, lastCamY, lastRangeX, lastRangeY;
    /** 当前绘制请求容器池尺寸*/
    private int requestidx = 0;
    /** 绘制瓦砾起始索引*/
    private int iterateidx = 0;
    /***/
    private float brokenFade = 0f;
    /** 阴影缓存*/
    private FrameBuffer shadows = new FrameBuffer(2, 2);
    /** 黑雾缓存*/
    private FrameBuffer fog = new FrameBuffer(2, 2);
    /***/
    private Array<Tile> outArray = new Array<>();
    /***/
    private Array<Tile> shadowEvents = new Array<>();

    public BlockRenderer(){

        for(int i = 0; i < requests.size; i++){     // 初始化块请求
            requests.set(i, new BlockRequest());
        }

        Events.on(WorldLoadEvent.class, event -> {
            shadowEvents.clear();
            lastCamY = lastCamX = -99; //invalidate camera position so blocks get updated

            shadows.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
            shadows.resize(world.width(), world.height());
            shadows.begin();
            Core.graphics.clear(Color.white);
            Draw.proj().setOrtho(0, 0, shadows.getWidth(), shadows.getHeight());

            Draw.color(shadowColor);

            for(int x = 0; x < world.width(); x++){
                for(int y = 0; y < world.height(); y++){
                    Tile tile = world.rawTile(x, y);
                    if(tile.block().hasShadow){
                        Fill.rect(tile.x + 0.5f, tile.y + 0.5f, 1, 1);
                    }
                }
            }

            Draw.flush();
            Draw.color();
            shadows.end();

            fog.getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);
            fog.resize(world.width(), world.height());
            fog.begin();
            Core.graphics.clear(Color.white);
            Draw.proj().setOrtho(0, 0, fog.getWidth(), fog.getHeight());

            for(int x = 0; x < world.width(); x++){
                for(int y = 0; y < world.height(); y++){
                    Tile tile = world.rawTile(x, y);
                    int edgeBlend = 2;
                    float rot = tile.rotation();
                    boolean fillable = (tile.block().solid && tile.block().fillsTile && !tile.block().synthetic());
                    int edgeDst = Math.min(x, Math.min(y, Math.min(Math.abs(x - (world.width() - 1)), Math.abs(y - (world.height() - 1)))));
                    if(edgeDst <= edgeBlend){
                        rot = Math.max((edgeBlend - edgeDst) * (4f / edgeBlend), fillable ? rot : 0);
                    }
                    if(rot > 0 && (fillable || edgeDst <= edgeBlend)){
                        Draw.color(0f, 0f, 0f, Math.min((rot + 0.5f) / 4f, 1f));
                        Fill.rect(tile.x + 0.5f, tile.y + 0.5f, 1, 1);
                    }
                }
            }

            Draw.flush();
            Draw.color();
            fog.end();
        });

        Events.on(TileChangeEvent.class, event -> {
            shadowEvents.add(event.tile);

            int avgx = (int)(camera.position.x / tilesize);
            int avgy = (int)(camera.position.y / tilesize);
            int rangex = (int)(camera.width / tilesize / 2) + 2;
            int rangey = (int)(camera.height / tilesize / 2) + 2;

            if(Math.abs(avgx - event.tile.x) <= rangex && Math.abs(avgy - event.tile.y) <= rangey){
                lastCamY = lastCamX = -99; //invalidate camera position so blocks get updated
            }
        });
    }

    /** 绘制黑雾*/
    public void drawFog(){
        float ww = world.width() * tilesize, wh = world.height() * tilesize;
        float x = camera.position.x + tilesize / 2f, y = camera.position.y + tilesize / 2f;
        float u = (x - camera.width / 2f) / ww,
        v = (y - camera.height / 2f) / wh,
        u2 = (x + camera.width / 2f) / ww,
        v2 = (y + camera.height / 2f) / wh;

        Tmp.tr1.set(fog.getTexture());
        Tmp.tr1.set(u, v2, u2, v);

        Draw.shader(Shaders.fog);
        Draw.rect(Tmp.tr1, camera.position.x, camera.position.y, camera.width, camera.height);
        Draw.shader();
    }

    /** 绘制销毁*/
    public void drawDestroyed(){
        if(!Core.settings.getBool("destroyedblocks")) return;

        if(control.input.isPlacing() || control.input.isBreaking()){
            brokenFade = Mathf.lerpDelta(brokenFade, 1f, 0.1f);
        }else{
            brokenFade = Mathf.lerpDelta(brokenFade, 0f, 0.1f);
        }

        if(brokenFade > 0.001f){
            for(BrokenBlock block : state.teams.get(player.getTeam()).brokenBlocks){
                Block b = content.block(block.block);
                if(!camera.bounds(Tmp.r1).grow(tilesize * 2f).overlaps(Tmp.r2.setSize(b.size * tilesize).setCenter(block.x * tilesize + b.offset(), block.y * tilesize + b.offset()))) continue;

                Draw.alpha(0.33f * brokenFade);
                Draw.mixcol(Color.white, 0.2f + Mathf.absin(Time.globalTime(), 6f, 0.2f));
                Draw.rect(b.icon(Cicon.full), block.x * tilesize + b.offset(), block.y * tilesize + b.offset(), b.rotate ? block.rotation * 90 : 0f);
            }
            Draw.reset();
        }
    }

    /** 绘制阴影*/
    public void drawShadows(){
        if(!shadowEvents.isEmpty()){
            Draw.flush();

            shadows.begin();
            Draw.proj().setOrtho(0, 0, shadows.getWidth(), shadows.getHeight());

            for(Tile tile : shadowEvents){
                //clear it first
                Draw.color(Color.white);
                Fill.rect(tile.x + 0.5f, tile.y + 0.5f, 1, 1);
                //then draw the shadow
                Draw.color(!tile.block().hasShadow ? Color.white : shadowColor);
                Fill.rect(tile.x + 0.5f, tile.y + 0.5f, 1, 1);
            }

            Draw.flush();
            Draw.color();
            shadows.end();
            shadowEvents.clear();

            Draw.proj(camera.projection());
        }

        float ww = world.width() * tilesize, wh = world.height() * tilesize;
        float x = camera.position.x + tilesize / 2f, y = camera.position.y + tilesize / 2f;
        float u = (x - camera.width / 2f) / ww,
        v = (y - camera.height / 2f) / wh,
        u2 = (x + camera.width / 2f) / ww,
        v2 = (y + camera.height / 2f) / wh;

        Tmp.tr1.set(shadows.getTexture());
        Tmp.tr1.set(u, v2, u2, v);

        Draw.shader(Shaders.fog);
        Draw.rect(Tmp.tr1, camera.position.x, camera.position.y, camera.width, camera.height);
        Draw.shader();
    }

    /** 处理绘制块.<p/>Process all blocks to draw. */
    public void processBlocks(){
        iterateidx = 0;
//        floorIterateidx = 0;

        int avgx = (int)(camera.position.x / tilesize);     // 相机X轴起始块
        int avgy = (int)(camera.position.y / tilesize);     // 相机Y轴起始块

        int rangex = (int)(camera.width / tilesize / 2) + 3;    // 相机显示块宽度
        int rangey = (int)(camera.height / tilesize / 2) + 3;   // 相机显示块高度

        if(avgx == lastCamX && avgy == lastCamY && lastRangeX == rangex && lastRangeY == rangey){   // 已更新过相机位置跳过
            return;
        }
        if (debug_blockdraw) {  // 绘制所有块
            avgx = avgy = 0;
            rangex = world.width();
            rangey = world.height();
        }

        requestidx = 0;
//        floorRequestidx = 0;

        int minx = Math.max(avgx - rangex - expandr, 0);    // 绘制瓦砾X轴位置
        int miny = Math.max(avgy - rangey - expandr, 0);    // 绘制瓦砾Y轴位置
        int maxx = Math.min(world.width() - 1, avgx + rangex + expandr);    // 绘制瓦砾X轴结束位置
        int maxy = Math.min(world.height() - 1, avgy + rangey + expandr);   // 绘制瓦砾Y轴结束位置
        if (debug_blockdraw) {
            minx = miny = 0;
            maxx = world.width() - 1;
            maxy = world.height() - 1;
        }

        if (enable_isoInput) {
//            floorRequests.clear();
            // 偏移像素单位. pixel offset of sub-tile in world-space
            float spx = ISOUtils.TILE_WIDTH * -0.5f;
            float spy = ISOUtils.TILE_HEIGHT *  -0;
            Rect rect = camera.bounds(Tmp.r3);
            rect.set(rect.x - spx, rect.y - spy, rect.width + spx * 2, rect.height + spy * 2);
            // 减去Draw绘制偏移量
            rect.setPosition(rect.x - ISOUtils.TILE_WIDTH50, rect.y - ISOUtils.TILE_HEIGHT50);
            Vec2 pos = ISOUtils.worldToTileCoords(rect.x, rect.y);

            avgx = (int) pos.x;    // 相机X轴起始块
            avgy = (int) pos.y;     // 相机Y轴起始块
            rangex = Mathf.ceil(rect.width / ISOUtils.TILE_WIDTH) + 2;     // default 2    // 相机显示块宽度
            rangey = Mathf.ceil(rect.height / ISOUtils.TILE_HEIGHT50) + 3;    // default 3  // 相机显示块高度
            boolean subY = (pos.x + pos.y) - ((int)pos.x + (int)pos.y) < tileunit;
            if (pos.y > pos.x) {
                if (subY) {
                    subY = false;
                    avgx--;
                } else {
                    avgy++;
                }
            }

            if(avgx == lastCamX && avgy == lastCamY && lastRangeX == rangex && lastRangeY == rangey){   // 已更新过相机位置跳过
                return;
            }

            for (int y = 0; y < rangey; y++) {
                int stx;
                int sty;
                stx = subY ? avgx : avgx++;
                sty = subY ? avgy-- : avgy;
                subY = !subY;

                for (int x = 0; x < rangex; x++) {
                    int tx = stx + x;
                    int ty = sty + x;

                    boolean expanded = false;
                    Tile tile = world.tile(tx, ty);
                    if(tile == null) continue; //how is this possible?
                    Block block = tile.block();

                    if(block != Blocks.air && block.cacheLayer == CacheLayer.normal){
                        if(!expanded){
                            addRequest(tile, Layer.block);
                        }

                        if(state.rules.lighting && tile.block().synthetic() && !(tile.block() instanceof BlockPart)){
                            addRequest(tile, Layer.lights);
                        }

                        if(block.expanded || !expanded){

                            // zones add begon
                            if (block.layerBg != null) {    // 最底层非缓存图层
                                addRequest(tile, block.layerBg);
                            }
                            // zones add end

                            if(block.layer != null){
                                addRequest(tile, block.layer);
                            }

                            if(block.layer2 != null){
                                addRequest(tile, block.layer2);
                            }

                            if(tile.entity != null && tile.entity.power != null && tile.entity.power.links.size > 0){
                                for(Tile other : block.getPowerConnections(tile, outArray)){
                                    if(other.block().layer == Layer.power){
                                        addRequest(other, Layer.power);
                                    }
                                }
                            }
                        }
                    }
                }
            }


//            for(int x = maxx; x >= minx; x--){
//                for(int y = miny; y <= maxy; y++){
//                    boolean expanded = (Math.abs(x - avgx) > rangex || Math.abs(y - avgy) > rangey);
//                    Tile tile = world.rawTile(x, y);
//                    if(tile == null) continue; //how is this possible?
//                    Block block = tile.block();
//
//                    if(block != Blocks.air && block.cacheLayer == CacheLayer.normal){
//                        if(!expanded){
//                            addRequest(tile, Layer.block);
//                        }
//
//                        if(state.rules.lighting && tile.block().synthetic() && !(tile.block() instanceof BlockPart)){
//                            addRequest(tile, Layer.lights);
//                        }
//
//                        if(block.expanded || !expanded){
//
//                            if(block.layer != null){
//                                addRequest(tile, block.layer);
//                            }
//
//                            if(block.layer2 != null){
//                                addRequest(tile, block.layer2);
//                            }
//
//                            if(tile.entity != null && tile.entity.power != null && tile.entity.power.links.size > 0){
//                                for(Tile other : block.getPowerConnections(tile, outArray)){
//                                    if(other.block().layer == Layer.power){
//                                        addRequest(other, Layer.power);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
        }
        else {
            for(int x = minx; x <= maxx; x++){
                for(int y = miny; y <= maxy; y++){
                    boolean expanded = (Math.abs(x - avgx) > rangex || Math.abs(y - avgy) > rangey);
                    Tile tile = world.rawTile(x, y);
                    if(tile == null) continue; //how is this possible?
                    Block block = tile.block();

                    if(block != Blocks.air && block.cacheLayer == CacheLayer.normal){
                        if(!expanded){
                            addRequest(tile, Layer.block);
                        }

                        if(state.rules.lighting && tile.block().synthetic() && !(tile.block() instanceof BlockPart)){
                            addRequest(tile, Layer.lights);
                        }

                        if(block.expanded || !expanded){

                            if(block.layer != null){
                                addRequest(tile, block.layer);
                            }

                            if(block.layer2 != null){
                                addRequest(tile, block.layer2);
                            }

                            if(tile.entity != null && tile.entity.power != null && tile.entity.power.links.size > 0){
                                for(Tile other : block.getPowerConnections(tile, outArray)){
                                    if(other.block().layer == Layer.power){
                                        addRequest(other, Layer.power);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if ( !disable_blockSort)
        Sort.instance().sort(requests.items, 0, requestidx);
        else if (enable_isoInput){  // zones add code begon
            // 地板绘制排序
//            Sort.instance().sort(floorRequests.items, 0, floorRequestidx);
            Sort.instance().sort(requests.items, 0, requestidx);
        }   // zones add code end

        lastCamX = avgx;
        lastCamY = avgy;
        lastRangeX = rangex;
        lastRangeY = rangey;
    }

    /** 绘制指定图层块瓦砾*/
    public void drawBlocks(Layer stopAt){
        int startIdx = iterateidx;
        for(; iterateidx < requestidx; iterateidx++){
            BlockRequest request = requests.get(iterateidx);

            if(request.layer.ordinal() > stopAt.ordinal()){     // 跳过多余图层
                break;
            }

            if(request.layer == Layer.power){   // 电力图层跳过
                if(iterateidx - startIdx > 0 && request.tile.pos() == requests.get(iterateidx - 1).tile.pos()){
                    continue;
                }
            }

            Block block = request.tile.block();

            if(request.layer == Layer.block){   // 绘制基础图层
                block.draw(request.tile);
                if(request.tile.entity != null && request.tile.entity.damaged()){       // 块损坏状态
                    block.drawCracks(request.tile);     // 绘制块损坏纹理
                }
                if(block.synthetic() && request.tile.getTeam() != player.getTeam()){    // 绘制块队伍图标
                    block.drawTeam(request.tile);
                }

            }
            // zones add begon
            else if(request.layer == Layer.background){    //  绘制光源
                block.drawBackground(request.tile);
            }
            // zones add end
            else if(request.layer == Layer.lights){    //  绘制光源
                block.drawLight(request.tile);
            }else if(request.layer == block.layer){     // 绘制块图层1
                block.drawLayer(request.tile);
            }else if(request.layer == block.layer2){    // 绘制块图层2
                block.drawLayer2(request.tile);
            }
        }
    }

    /** 添加绘制请求瓦砾*/
    private void addRequest(Tile tile, Layer layer){
        if(requestidx >= requests.size){
            requests.add(new BlockRequest());
        }
        BlockRequest r = requests.get(requestidx);
        if(r == null){
            requests.set(requestidx, r = new BlockRequest());
        }
        r.tile = tile;
        r.layer = layer;
        requestidx++;
        // zones add begon
        r.sortValue = (world.height() - tile.y - 1) * world.width() + tile.x;
        // zones add end
    }

    @Override
    public void dispose(){
        shadows.dispose();
        fog.dispose();
        shadows = fog = null;
        floor.dispose();
    }


    /**
     *  绘制请求块
     * */
    private class BlockRequest implements Comparable<BlockRequest>{
        /** 绘制瓦砾*/
        Tile tile;
        /** 图层*/
        Layer layer;
        // zones add begon
        /** 斜45地图绘制排序值*/
        int sortValue;
        // zones add end

        @Override
        public int compareTo(BlockRequest other){
            int compare = layer.compareTo(other.layer);

//            return (compare != 0) ? compare : Integer.compare(tile.pos(), other.tile.pos());
            return (compare != 0) ? compare : other.sortValue - sortValue;
        }

        @Override
        public String toString(){
            return tile.block().name + ":" + layer.toString();
        }
    }


    // zones add begon
    public final DebugFloorRenderer floorDebug = new DebugFloorRenderer();
//    /** 当前绘制请求容器池尺寸*/
//    private int floorRequestidx = 0;
//    /** 绘制瓦砾起始索引*/
//    private int floorIterateidx = 0;
//    /** 斜45地板索引*/
//    private Array<FloorRequest> floorRequests = new Array<>(true, initialRequests, FloorRequest.class);

    /** 斜45瓦砾绘制排序*/
    private Comparator comparator = new Comparator<BlockRequest>() {   //    r.sortValue = (world.height() - tile.y - 1) * world.width() + tile.x;
        @Override
        public int compare(BlockRequest request1, BlockRequest request2) {
            return ((world.height() - request2.tile.y - 1) * world.width() + request2.tile.x) - ((world.height() - request1.tile.y - 1) * world.width() + request1.tile.x);
        }
    };


//    private class FloorRequest implements Comparable<FloorRequest>{
//        int sortKey;
//        Tile tile;
//
//        @Override
//        public int compareTo(FloorRequest other){
//            return other.sortKey - sortKey;
//        }
//    }
    // zones add end
}
