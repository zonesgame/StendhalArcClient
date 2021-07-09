package z.world.blocks.defense;

import arc.Core;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import mindustry.world.Tile;
import z.entities.type.UpgradeTileEntity;

/**
 *  权倾天下块提供绘制功能
 * */
public abstract  class DrawBlock extends UpgradeBlock {

    protected Cons<Tile> drawerEffect = null;

    public DrawBlock(String name) {
        super(name);
    }

    @Override
    public void load(){
        super.load();
        initExtendRegions();
    }

    @Override
    public void draw(Tile tile){
        Vec2 pos = Vec2.TEMP2;
//        ISOUtils.tileToWorldCoordsCenter(tile.x, tile.y, size, size, pos);
        pos.set(tile.drawxIso(), tile.drawyIso());

        float scale = 1;
        float dx = pos.x - bgRect.x;
        float dy = pos.y - bgRect.y;
        Draw.rectGdx(bgRegion, dx, dy, bgRect.width, bgRect.height);

        UpgradeTileEntity entity = tile.ent();
        if (entity.isDrag)  return;     // 编辑移动状态

        int lev = entity.level();
        int curVariant = entity.curVariant;
        region = upgradeRegions[lev][curVariant][0];
        rect = upgradeRects[lev][curVariant][0];
        dx = pos.x - rect.x;
        dy = pos.y - rect.y;
        Draw.rectGdx(region, dx, dy, rect.width, rect.height);

        if (drawerEffect != null)
            drawerEffect.get(tile);
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(Core.atlas.has(name) ? name : name + "1")};
    }

    // zones add begon
//    private int curVariant = 0;
    @Deprecated
    /** 临时数据*/
    protected String[] resourceFoled;
    /** index 0 animation class, index 1 animation type, index 2 animation frame*/
    private TextureRegion[][][] upgradeRegions;         //
    /***/
    private Rect[][][] upgradeRects;    //
    /***/
    protected Rect rect;
    /** 背景数据*/
    private TextureRegion bgRegion;
    protected Rect bgRect;

    private void initExtendRegions() {
//        String[] resourceFoled = {   // 临时资源列表正式版将有构造方法初始化
//                "102021", "102022", "102023", "102024", "102025", "102026"
//        };





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
    }

    // zones add end
}
