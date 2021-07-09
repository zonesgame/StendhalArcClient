//package z.test;
//
//import arc.graphics.Color;
//import arc.graphics.g2d.Draw;
//import arc.graphics.g2d.Lines;
//import arc.graphics.g2d.TextureRegion;
//import arc.math.Mathf;
//import arc.math.geom.Vec2;
//import mindustry.world.Block;
//import mindustry.world.Tile;
//import mindustry.world.meta.BlockGroup;
//import z.test.camera.EngineUtils;
//
//import static arc.Core.atlas;
//import static arc.graphics.g2d.Draw.scl;
//import static z.test.camera.EngineUtils.TILE_HEIGHT50;
//
///**
// *  测试斜45块
// */
//public class ISOBlock extends Block {
//    public int variants = 0;
//
//    public ISOBlock(String name){
//        super(name);
//        solid = true;
//        destructible = true;
//        group = BlockGroup.walls;
//        buildCostMultiplier = 5f;
//    }
//
//    @Override
//    public void load(){
//        super.load();
//
//        if(variants != 0){
//            variantRegions = new TextureRegion[variants];
//
//            for(int i = 0; i < variants; i++){
//                variantRegions[i] = atlas.find(name + (i + 1));
//            }
//            region = variantRegions[0];
//        }
//        // zones add begon
//        if (tiles == null) {
//            initRegions();
//        }
//        // zones add edn
//    }
//
//    @Override
//    public void draw(Tile tile){
//        // zones add begon
//        if (true) {
//            region = tiles[0];
//            Vec2 center = regionCenter[0];
//            EngineUtils.tileToWorldCoordsCenter(tile.x, tile.y, 1, 1, pos);
//
//            float scale = scl;
//            float _x = (pos.x - center.x) * scale;
//            float _y = (pos.y - center.y) * scale;
//            float _w = region.getWidth() * scale;
//            float _h = region.getHeight() * scale;
//            Draw.rectGdx(region, _x, _y, _w, _h);
//            return;
//        }
//
//        if (true) {
//            float addx = 200;
//            float addy = 100;
//            TextureRegion region = tiles[0];
//
//            region = tiles[0];
//            EngineUtils.tileToWorldCoordsCenter(4, 3, 1, 1, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,1));
//            EngineUtils.tileToWorldCoordsCenter(3, 4, 1, 1, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,1));
//            EngineUtils.tileToWorldCoordsCenter(4, 4, 1, 1, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,1));
//            EngineUtils.tileToWorldCoordsCenter(3, 3, 1, 1, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,1));
//
//            region = tiles[1];
//            EngineUtils.tileToWorldCoordsCenter(0, 0, 2, 2, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,2));
//
//            region = tiles[2];
//            EngineUtils.tileToWorldCoordsCenter(2, 0, 3, 3, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,3));
//
//            region = tiles[3];
//            EngineUtils.tileToWorldCoordsCenter(5, 0, 4, 4, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,4));
//
//            region = tiles[4];
//            EngineUtils.tileToWorldCoordsCenter(9, 0, 2, 1, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,1));
//
//            region = tiles[5];
//            EngineUtils.tileToWorldCoordsCenter(9, 1, 1, 2, pos);
//            Draw.rectGdxScale(region, addx + pos.x, addy + pos.y - getOffsetY(region,2));
//
//            if (true) {
//                Draw.color(Color.blue);
//                Lines.stroke(0.1f);
//                drawDebug(4, 3, 1, 1, addx, addy);
//                drawDebug(3, 4, 1, 1, addx, addy);
//                drawDebug(3, 3, 1, 1, addx, addy);
//                drawDebug(4, 4, 1, 1, addx, addy);
//
//                drawDebug(0, 0, 2, 2, addx, addy);
//                drawDebug(2, 0, 3, 3, addx, addy);
//                drawDebug(5, 0, 4, 4, addx, addy);
//                drawDebug(9, 0, 2, 1, addx, addy);
//                drawDebug(9, 1, 1, 2, addx, addy);
//                Draw.reset();
//            }
//
//            return;
//        }
//        // zones add end
//
//        if(variants == 0){
//            Draw.rect(region, tile.drawx(), tile.drawy());
//        }else{
//            Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.drawx(), tile.drawy());
//        }
//    }
//
//    @Override
//    public TextureRegion[] generateIcons(){
//        return new TextureRegion[]{atlas.find(atlas.has(name) ? name : name + "1")};
//    }
//
//    @Override
//    public boolean canReplace(Block other){
//        return super.canReplace(other) && health > other.health;
//    }
//
//    // zones add begon
//    private void drawDebug(float tilex, float tiley, float tilew, float tileh, float addx, float addy) {
//        float scale = scl;
////        Draw.color(Color.green);
//        Vec2 start = new Vec2(), end = new Vec2(), start2 = new Vec2();
//        EngineUtils.tileToWorldCoords(tilex, tiley, start);
//        EngineUtils.tileToWorldCoords(tilex + tilew, tiley + tileh, start2);
//        EngineUtils.tileToWorldCoords(tilex + tilew, tiley, end);
//        Lines.line((start.x + addx) * scale, (start.y + addy) * scale, (end.x + addx) * scale, (end.y + addy) * scale);
//        Lines.line((start2.x + addx) * scale, (start2.y + addy) * scale, (end.x + addx) * scale, (end.y + addy) * scale);
//        EngineUtils.tileToWorldCoords(tilex, tiley + tileh, end);
//        Lines.line((start.x + addx) * scale, (start.y + addy) * scale, (end.x + addx) * scale, (end.y + addy) * scale);
//        Lines.line((start2.x + addx) * scale, (start2.y + addy) * scale, (end.x + addx) * scale, (end.y + addy) * scale);
////        Draw.reset();
//    }
//
//    private float getOffsetY(TextureRegion region, float tileHeight) {
//        if (true)   return 0;
////        return region.getHeight() / 2f;
//        return TILE_HEIGHT50 * tileHeight;
//    }
//
//    private void initRegions() {
//        tiles = new TextureRegion[6];
//        for (int i = 0; i < tiles.length; i++) {
//            tiles[i] = atlas.find("isoTile" + i);
//        }
//
//        walls = new TextureRegion[8];
//        for (int i = 0; i < walls.length; i++) {
//            walls[i] = atlas.find("isoWall" + i);
//        }
//
//        // 0 43,34
//        regionCenter = new Vec2[6];
//        {
//            regionCenter[0] = new Vec2(43,34);
//            regionCenter[1] = new Vec2(79,61);
//            regionCenter[2] = new Vec2(118,94);
//            regionCenter[3] = new Vec2(157,121);
//            regionCenter[4] = new Vec2(61,46);
//            regionCenter[5] = new Vec2(61,49);
//        }
//    }
//
//    private Vec2 pos = new Vec2();
//    private TextureRegion[] tiles, walls;
//    private Vec2[] regionCenter;
//    // zones add end
//}
