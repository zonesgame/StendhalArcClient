package z.world.blocks.defense;

import arc.Core;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.Autotiler;
import mindustry.world.meta.BlockGroup;
import z.entities.type.UpgradeTileEntity;
import z.tools.serialize.XmlSerialize;
import z.utils.ShapeRenderer;

import static z.debug.ZDebug.disable_packLoad;

/**
 *  斜45墙体
 * */
public class WallIso extends UpgradeBlock implements Autotiler {

    protected Cons<Tile> drawerEffect = null;

    public WallIso(String name){
        super(name);
        solid = true;
        destructible = true;
        group = BlockGroup.walls;
        buildCostMultiplier = 5f;
        // zones add begon
        entityType = UpgradeTileEntity::new;
        configurable = true;
        unloadable = false;
        // zones add end
        this.layerBg = Layer.background;
    }

    private String configFile;
    public WallIso(String name, String configName){
        this(name);
        this.configFile = configName;
        Vars.xmlSerialize.loadBlockConfig(configName, this);
    }

    @Override
    public void load(){
        super.load();
        // zones add begon
        if (disable_packLoad)   return;
        ObjectMap dataPool = Vars.xmlSerialize.loadBlockAnimation(configFile);
        upgradeRegions = (TextureRegion[][][]) dataPool.get(XmlSerialize.qqtxRegions);
        upgradeRects = (Rect[][][]) dataPool.get(XmlSerialize.qqtxRects);
        bgRegion = (TextureRegion) dataPool.get(XmlSerialize.qqtxBG);
        bgRect = (Rect) dataPool.get(XmlSerialize.qqtxBGR);
        // zones add end
    }


    @Override
    public boolean canReplace(Block other){
        return super.canReplace(other) && health[0] > other.health[0];
    }

    // Conveyor add begon
    @Override
    public void onProximityUpdate(Tile tile){
        super.onProximityUpdate(tile);

        int[] bits = buildBlendingSG(tile, /*tile.rotation()*/0, null, true);
        UpgradeTileEntity entity = tile.ent();
        entity.curVariant = bits[0];
    }

    @Override
    public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock) {
        boolean returnvalue = (otherblock instanceof WallIso);
        return returnvalue;
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(Core.atlas.has(name) ? name : name + "1")};
    }

    @Override
    public void draw(Tile tile){
        ShapeRenderer.drawDiamond(tile.x, tile.y, size);
        Vec2 pos = Vec2.TEMP2;
//        ISOUtils.tileToWorldCoordsCenter(tile.x, tile.y, size, size, pos);
        pos.set(tile.drawxIso(), tile.drawyIso());

        float scale = 1;
        float dx = pos.x - bgRect.x;
        float dy = pos.y - bgRect.y;
//        Draw.rectGdx(bgRegion, dx, dy, bgRect.width, bgRect.height);

        UpgradeTileEntity entity = tile.ent();
        if (entity.isDrag)  return;     // 编辑移动状态

        int lev = entity.level();
        int curVariant = entity.curVariant;
        region = upgradeRegions[lev][curVariant][0];
        rect = upgradeRects[lev][curVariant][0];
//        dx = pos.x - rect.x;
//        dy = pos.y - rect.y;
        Draw.rectGdx(region, pos.x, pos.y, rect);

        if (drawerEffect != null)
            drawerEffect.get(tile);
    }

    @Override
    public void drawBackground(Tile tile) {
        Vec2 pos = Vec2.TEMP2;
//        ISOUtils.tileToWorldCoordsCenter(tile.x, tile.y, size, size, pos);
        pos.set(tile.drawxIso(), tile.drawyIso());

        float scale = 1;
        float dx = pos.x - bgRect.x;
        float dy = pos.y - bgRect.y;
        Draw.rectGdx(bgRegion, pos.x, pos.y, bgRect);
    }


    // zones add begon
//    private int curVariant = 0;
    /** index 0 animation class, index 1 animation type, index 2 animation frame*/
    private TextureRegion[][][] upgradeRegions;         //
    /***/
    private Rect[][][] upgradeRects;    //
    /***/
    protected Rect rect;
    /** 背景数据*/
    private TextureRegion bgRegion;
    protected Rect bgRect;

//    private void initExtendRegions() {
////        String[] resourceFoled = {   // 临时资源列表正式版将有构造方法初始化
////                "102021", "102022", "102023", "102024", "102025", "102026"
////        };
//        this.buildName = resourceFoled[0];
//        maxLevel = resourceFoled.length;
//        upgradeRegions = new TextureRegion[maxLevel][][];
//        upgradeRects = new Rect[maxLevel][][];
//        PackLoader loader = PackLoader.getInstance();
//        int index = 0;
//        for (String foled : resourceFoled) {
//            upgradeRegions[index] = loader.packs.get(foled);
//            upgradeRects[index++] = loader.rects.get(foled);
//        }
//        // 背景数据初始化
//        bgRect = loader.rects.get("1")[0][size-1];
//        bgRegion =  loader.packs.get("1")[0][size-1];
//    }
    // zones add end
}
