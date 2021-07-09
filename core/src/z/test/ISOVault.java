//package z.test;
//
//import arc.graphics.g2d.Draw;
//import arc.graphics.g2d.TextureRegion;
//import arc.math.geom.Rect;
//import arc.math.geom.Vec2;
//import arc.z.util.ISOUtils;
//import mindustry.world.Tile;
//import mindustry.world.blocks.storage.Vault;
//import z.debug.assets.PackLoader;
//
///**
// *
// */
//public class ISOVault extends Vault {
//
//    private TextureRegion bgRegion, buildRegion, buildupRegion;
//    private Rect bgRect, buildRect, buildupRect;
//    protected String buildRegionID, buildupRegionID;
//
//    public ISOVault(String name) {
//        super(name);
//    }
//
//    @Override
//    public void load(){
//        super.load();
//
//        PackLoader loader = PackLoader.getInstance();
//        bgRect = loader.rects.get("1")[0][size-1];
//        bgRegion =  loader.packs.get("1")[0][size-1];
//
//        if (buildRegionID != null) {
//            buildRect = loader.rects.get(buildRegionID)[0][0];
//            buildRegion =  loader.packs.get(buildRegionID)[0][0];
//        }
//        if (buildupRegionID != null) {
//            buildupRect = loader.rects.get(buildupRegionID)[0][0];
//            buildupRegion =  loader.packs.get(buildupRegionID)[0][0];
//        }
//    }
//
//    Vec2 pos = new Vec2();
//    @Override
//    public void draw(Tile tile){
//        ISOUtils.tileToWorldCoordsCenter(tile.x, tile.y, size, size, pos);
////        EngineUtils.tileToWorldCoords(tile.x, tile.y, pos);
//
//        float scale = 1;
//        float _x = (pos.x - bgRect.x) * scale;
//        float _y = (pos.y - bgRect.y) * scale;
//        float _w = bgRect.getWidth() * scale;
//        float _h = bgRect.getHeight() * scale;
//        Draw.rectGdx(bgRegion, _x, _y, _w, _h);
//
//        if (buildRect != null) {
//            _x = (pos.x - buildRect.x) * scale;
//            _y = (pos.y - buildRect.y) * scale;
//            _w = buildRect.getWidth() * scale;
//            _h = buildRect.getHeight() * scale;
//            Draw.rectGdx(buildRegion, _x, _y, _w, _h);
//        }
//
//        Draw.color();
//    }
//
//    @Override
//    public void drawLayer(Tile tile){
////        float scale = scl;
////        float _x;
////        float _y;
////        float _w;
////        float _h;
////        if (buildRect != null) {
////            _x = (pos.x - buildRect.x) * scale;
////            _y = (pos.y - buildRect.y) * scale;
////            _w = buildRect.getWidth() * scale;
////            _h = buildRect.getHeight() * scale;
////            Draw.rectGdx(buildRegion, _x, _y, _w, _h);
////            System.out.println("OOOOOOOOOOOOOOOO");
////        } else {
////            System.out.println("nullllllllllllllllllllllllll");
////        }
//    }
//
//}
