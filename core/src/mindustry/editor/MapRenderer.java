package mindustry.editor;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.IntSet;
import arc.struct.IntSet.IntSetIterator;
import arc.util.Disposable;
import arc.z.util.ISOUtils;
import mindustry.content.Blocks;
import mindustry.game.EventType.ContentReloadEvent;
import mindustry.game.Team;
import mindustry.graphics.IndexedRenderer;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.BlockPart;

import static arc.z.util.ISOUtils.TILE_HEIGHT50;
import static mindustry.Vars.tilesize;
import static z.debug.ZDebug.enable_editorIso;
import static z.debug.ZDebug.enable_isoInput;

/**
 *  编辑器地图绘制器
 * */
public class MapRenderer implements Disposable{
    private static final int chunkSize = 64;
    private IndexedRenderer[][] chunks;
    private IntSet updates = new IntSet();
    private IntSet delayedUpdates = new IntSet();
    private MapEditor editor;
    private int width, height;
    private Texture texture;

    public MapRenderer(MapEditor editor){
        this.editor = editor;
        this.texture = Core.atlas.find("clear-editor").getTexture();

        Events.on(ContentReloadEvent.class, e -> {
            texture = Core.atlas.find("clear-editor").getTexture();
        });
    }

    public void resize(int width, int height){
        if(chunks != null){
            for(int x = 0; x < chunks.length; x++){
                for(int y = 0; y < chunks[0].length; y++){
                    chunks[x][y].dispose();
                }
            }
        }

        chunks = new IndexedRenderer[(int)Math.ceil((float)width / chunkSize)][(int)Math.ceil((float)height / chunkSize)];

        for(int x = 0; x < chunks.length; x++){
            for(int y = 0; y < chunks[0].length; y++){
                chunks[x][y] = new IndexedRenderer(chunkSize * chunkSize * 3);      // 3三个图层1. floor, 2. block, 3. 队伍图标
            }
        }
        this.width = width;
        this.height = height;
        updateAll();
    }

    public void draw(float tx, float ty, float tw, float th){
        if (enable_isoInput && false) {
            Draw.shader();
            Draw.reset();
            for(int x = 0; x < width; x++){
                for(int y = 0; y < height; y++){
                    Tile tile = editor.tiles()[x][y];
                    if (tile.floor() != null)
                        tile.floor().draw(tile);
                    if (tile.block() != null)
                        tile.block().draw(tile);
                }
            }
            return;
        }
        Draw.flush();

        IntSetIterator it = updates.iterator();
        while(it.hasNext){
            int i = it.next();
            int x = i % width;
            int y = i / width;
            render(x, y);
        }
        updates.clear();

        updates.addAll(delayedUpdates);
        delayedUpdates.clear();

        //????
        if(chunks == null){
            return;
        }

        for(int x = 0; x < chunks.length; x++){
            for(int y = 0; y < chunks[0].length; y++){
                IndexedRenderer mesh = chunks[x][y];

                if(mesh == null){
                    continue;
                }

                mesh.getTransformMatrix().setToTranslation(tx, ty).scale(tw / (width * tilesize), th / (height * tilesize));
                mesh.setProjectionMatrix(Draw.proj());

                mesh.render(texture);
            }
        }
    }

    public void updatePoint(int x, int y){
        updates.add(x + y * width);
    }

    public void updateAll(){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                render(x, y);
            }
        }
    }

    /** 绘制指定瓦砾*/
    private void render(int wx, int wy){
        int x = wx / chunkSize, y = wy / chunkSize;
        IndexedRenderer mesh = chunks[x][y];
        Tile tile = editor.tiles()[wx][wy];

        Team team = tile.getTeam();
        Block floor = tile.floor();
        Block wall = tile.block();

        TextureRegion region;

        int idxFloor = (wx % chunkSize) + (wy % chunkSize) * chunkSize;
        int idxBlock = (wx % chunkSize) + (wy % chunkSize) * chunkSize + chunkSize * chunkSize;
        int idxDecal = (wx % chunkSize) + (wy % chunkSize) * chunkSize + chunkSize * chunkSize * 2;

        if (enable_editorIso) {       //  始终绘制地板
            region = floor.editorVariantRegions()[0];
//            Vec2 wpos = ISOUtils.tileToWorldCoords(wx, wy).sub(0, TILE_HEIGHT50 * floor.size);
            Vec2 wpos = ISOUtils.tileToWorldCoords(wx - ((floor.size - 1) / 2), wy - ((floor.size - 1) / 2)).sub(0, TILE_HEIGHT50 * floor.size);
            mesh.draw(idxFloor, region, wpos.x, wpos.y, region.getWidth() * Draw.scl, region.getHeight() * Draw.scl);
        }

        if(wall != Blocks.air && (wall.synthetic() || wall instanceof BlockPart)){
            region = !Core.atlas.isFound(wall.editorIcon()) ? Core.atlas.find("clear-editor") : wall.editorIcon();

            if(wall.rotate){
                if (enable_editorIso) {
//                    Vec2 wpos = ISOUtils.tileToWorldCoords(wx + wall.offsetTile(), wy + wall.offsetTile());
//                    mesh.draw(idxBlock, region, wpos.x, wpos.y, region.getWidth() * Draw.scl, region.getHeight() * Draw.scl, tile.rotation() * 90 - 90);
                    Vec2 wpos;
                    if ( !wall.regionCenter(0).isZero()) {     //  绘制原图, 使用中心点
                        wpos = ISOUtils.tileToWorldCoords(wall.offsetTile() + wx + (wall.size / 2f), wall.offsetTile() + wy + (wall.size / 2f));
                        wpos.sub(wall.regionCenter(0).x * Draw.scl, wall.regionCenter(0).y * Draw.scl);
                    } else {    //  仅绘制编辑器提供的图标
                        wpos = ISOUtils.tileToWorldCoords(wx - ((wall.size - 1) / 2), wy - ((wall.size - 1) / 2)).sub(0, TILE_HEIGHT50 * wall.size);
                    }

                    mesh.draw(idxBlock, region, wpos.x, wpos.y, region.getWidth() * Draw.scl, region.getHeight() * Draw.scl);       // zones disable rotation
                } else {
                    mesh.draw(idxBlock, region,
                            wx * tilesize + wall.offset(), wy * tilesize + wall.offset(),
                            region.getWidth() * Draw.scl, region.getHeight() * Draw.scl, tile.rotation() * 90 - 90);
                }
            }else{
                float width = region.getWidth() * Draw.scl, height = region.getHeight() * Draw.scl;

                if (enable_editorIso) {     // 建筑绘制
                    Vec2 wpos;
                    if ( !wall.regionCenter(0).isZero()) {     //  绘制原图, 使用中心点
                        wpos = ISOUtils.tileToWorldCoords(wall.offsetTile() + wx + (wall.size / 2f), wall.offsetTile() + wy + (wall.size / 2f));
                        wpos.sub(wall.regionCenter(0).x * Draw.scl, wall.regionCenter(0).y * Draw.scl);
                    } else {    //  仅绘制编辑器提供的图标
                        wpos = ISOUtils.tileToWorldCoords(wx - ((wall.size - 1) / 2), wy - ((wall.size - 1) / 2)).sub(0, TILE_HEIGHT50 * wall.size);
                    }

                    mesh.draw(idxBlock, region, wpos.x, wpos.y, region.getWidth() * Draw.scl, region.getHeight() * Draw.scl);
                } else {
                    mesh.draw(idxBlock, region,
                            wx * tilesize + wall.offset() + (tilesize - width) / 2f,
                            wy * tilesize + wall.offset() + (tilesize - height) / 2f,
                            width, height);
                }
            }
        }
//        else{
//            region = floor.editorVariantRegions()[Mathf.randomSeed(idxFloor, 0, floor.editorVariantRegions().length - 1)];
//            if (enable_editorIso) {
//                region = floor.editorVariantRegions()[0];
//                Vec2 wpos = ISOUtils.tileToWorldCoords(wx, wy).sub(0, TILE_HEIGHT50);
////                        .sub(TILE_WIDTH50, TILE_HEIGHT50).add(tilesize / 2f, tilesize / 2f);
//                mesh.draw(idxFloor, region, wpos.x, wpos.y, region.getWidth() * Draw.scl, region.getHeight() * Draw.scl);
//            } else {
//                mesh.draw(idxFloor, region, wx * tilesize, wy * tilesize, 8, 8);
//            }
//        }

        else if ( !wall.synthetic() && wall != Blocks.air) {   // zones add 静态块绘制
            region = !Core.atlas.isFound(wall.editorIcon()) ? Core.atlas.find("clear-editor") : wall.editorIcon();

            Vec2 wpos;
            if ( !wall.regionCenter(0).isZero()) {     //  绘制原图, 使用中心点
                wpos = ISOUtils.tileToWorldCoords(wall.offsetTile() + wx + (wall.size / 2f), wall.offsetTile() + wy + (wall.size / 2f));
                wpos.sub(wall.regionCenter(0).x * Draw.scl, wall.regionCenter(0).y * Draw.scl);
            } else {    //  仅绘制编辑器提供的图标
                wpos = ISOUtils.tileToWorldCoords(wx - ((wall.size - 1) / 2), wy - ((wall.size - 1) / 2)).sub(0, TILE_HEIGHT50 * wall.size);
            }

            mesh.draw(idxBlock, region, wpos.x, wpos.y, region.getWidth() * Draw.scl, region.getHeight() * Draw.scl);
        }
        else {      // 清空绘制缓存
            region = Core.atlas.find("clear-editor");
            mesh.draw(idxBlock, region, 0, 0, 0, 0);
        }

        float offsetX = -(wall.size / 3) * tilesize, offsetY = -(wall.size / 3) * tilesize;

        if(wall.update || wall.destructible){
            mesh.setColor(team.color);
            region = Core.atlas.find("block-border-editor");
            if (enable_editorIso) region = Core.atlas.find("block-border-editorIso");
        }
//        else if(!wall.synthetic() && wall != Blocks.air){
//            region = !Core.atlas.isFound(wall.editorIcon()) ? Core.atlas.find("clear-editor") : wall.editorIcon();
//            offsetX = tilesize / 2f - region.getWidth() / 2f * Draw.scl;
//            offsetY = tilesize / 2f - region.getHeight() / 2f * Draw.scl;
//        }
        else if(wall == Blocks.air && tile.overlay() != null){
            region = tile.overlay().editorVariantRegions()[Mathf.randomSeed(idxFloor, 0, tile.overlay().editorVariantRegions().length - 1)];
        }else{
            region = Core.atlas.find("clear-editor");
        }

        float width = region.getWidth() * Draw.scl, height = region.getHeight() * Draw.scl;
        if(!wall.synthetic() && wall != Blocks.air && !wall.isMultiblock()){
            offsetX = 0;
            offsetY = 0;
            width = tilesize;
            height = tilesize;
        }

        if (enable_editorIso) {
            Vec2 wpos = ISOUtils.tileToWorldCoords(wx - ((wall.size - 1) / 2), wy - ((wall.size - 1) / 2) ).sub(0, TILE_HEIGHT50 * wall.size);
            mesh.draw(idxDecal, region, wpos.x, wpos.y, region.getWidth() * Draw.scl * wall.size, region.getHeight() * Draw.scl * wall.size);
        }
        else {
            mesh.draw(idxDecal, region, wx * tilesize + offsetX, wy * tilesize + offsetY, width, height);
        }
        mesh.setColor(Color.white);
    }

    @Override
    public void dispose(){
        if(chunks == null){
            return;
        }
        for(int x = 0; x < chunks.length; x++){
            for(int y = 0; y < chunks[0].length; y++){
                if(chunks[x][y] != null){
                    chunks[x][y].dispose();
                }
            }
        }
    }
}
