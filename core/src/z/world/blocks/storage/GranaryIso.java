package z.world.blocks.storage;

import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.ObjectMap;
import arc.z.util.ZonesAnnotate.ZAdd;
import mindustry.Vars;
import mindustry.entities.type.TileEntity;
import mindustry.graphics.Layer;
import mindustry.type.Item;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import z.entities.type.UpgradeTileEntity;
import z.tools.serialize.XmlSerialize;
import z.ui.SettingDisplay;
import z.utils.ShapeRenderer;
import z.world.blocks.OperationAction;
import z.world.modules.ItemModuleCustom;

import static mindustry.Vars.control;
import static mindustry.Vars.systemItems;
import static z.debug.ZDebug.disable_itemFilter;
import static z.debug.ZDebug.disable_packLoad;

/**
 *  粮仓  凯撒
 * */
public class GranaryIso extends StorageBlockIso{

    /** 块数据*/
    private TextureRegion[][][] upgradeRegions;         //
    private Rect[][][] upgradeRects;    //
    /**动画数据*/
    private TextureRegion[][] aniRegions;         //
    private Rect[][] aniRects;    //
    /** 背景数据*/
    private TextureRegion bgRegion;
    private Rect bgRect;

    /**temp value*/
    private Rect rect;
    private TextureRegion region_;

    public GranaryIso(String name){
        super(name);
        solid = true;
        update = true;
        destructible = true;
        // zones test code begon
        flags = EnumSet.of(BlockFlag.granary);
//        acceptType.sort(Structs.comparingInt(i -> i.id));       // ui显示数据排序
        entityType = GranaryIsoEntity::new;
        layerBg = Layer.background;     // 绘制权倾天下地板块
        layer2 = null;  // 禁止绘制工作状态图表
        // zones test code end
    }

    private String configFile;
    public GranaryIso(String name, String xmlFile){
        this(name);
        this.configFile = xmlFile;
        Vars.xmlSerialize.loadBlockConfig(xmlFile, this);
    }

    @Override
    public void load(){
        super.load();

        if (disable_packLoad)   return;
        ObjectMap dataPool = Vars.xmlSerialize.loadBlockAnimation(configFile);
        upgradeRegions = (TextureRegion[][][]) dataPool.get(XmlSerialize.qqtxRegions);
        upgradeRects = (Rect[][][]) dataPool.get(XmlSerialize.qqtxRects);
        aniRegions = ((TextureRegion[][][]) dataPool.get(XmlSerialize.qqtxANI))[0];
        aniRects = ((Rect[][][]) dataPool.get(XmlSerialize.qqtxANIR))[0];
        bgRegion = (TextureRegion) dataPool.get(XmlSerialize.qqtxBG);
        bgRect = (Rect) dataPool.get(XmlSerialize.qqtxBGR);
        dataPool.clear();   //
    }

    @ZAdd
    @Override
    public boolean acceptItemCaesar(Item item, Tile tile, Tile source){
        ItemModuleCustom acceptItemConfig = ((GranaryIsoEntity) tile.ent()).acceptItemConfig;       // no exception check
        return acceptItem(item, tile, source)
                && acceptItemConfig.stateAccept(item)
                && tile.entity.items.get(item) < acceptItemConfig.get(item);
    }

    @ZAdd
    @Override
    public boolean getItemCaesar(Item item, Tile tile, Tile source){
        ItemModuleCustom acceptItemConfig = ((GranaryIsoEntity) tile.ent()).acceptItemConfig;       // no exception check
        return acceptItem(item, tile, source)
                && acceptItemConfig.stateGet(item)
                && tile.entity.items.get(item) < acceptItemConfig.get(item);
    }

    /** 是否可接收指定物品*/
    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return (disable_itemFilter || consumes[tile.entity.level()].itemFilters.get(item.id))
                && systemItems.getGranaryAccept().contains(item)
                && tile.entity.items.total() < getMaximumAccepted(tile, item);
    }

    /** 仓库数据将会进行类别区分*/
    @Override
    public int getMaximumAccepted(Tile tile, Item item){
        return itemCapacity[tile.entity.level()];
    }

    @Override
    public void update(Tile tile) {         // update == true
        super.update(tile);

        GranaryIsoEntity entity = tile.ent();
        if(!entity.setWorkState || !entity.working)   // 无法执行工作 退出
            return;

        if(tile.entity.timer.get(timerWorker, workerTime * 2) && entity.offerWorker()){     //  移除清空物品
//            GranaryIsoEntity entity = tile.ent();

            // 清除多余物品
//            for (ItemStack itemStack : entity.warehouseContent) {
//                if (itemStack.amount > 0 && entity.acceptItemConfig.stateClear(itemStack.item)) {
//                    tryDump(tile, itemStack.item);
////                    entity.removeItem(itemStack.item, 1);
//                    return;
//                }
//            }

            // 补给获取物品
        }
    }

    @Override
    public void draw(Tile tile){
        ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);

        Vec2 pos = Vec2.TEMP2;
        pos.set(tile.drawxIso(), tile.drawyIso());

        UpgradeTileEntity entity = tile.ent();
        if (entity.isDrag)  return;     // 编辑移动状态

        int lev = entity.level();
        int curVariant = entity.curVariant;
        region_ = upgradeRegions[lev][curVariant][0];
        rect = upgradeRects[lev][curVariant][0];
        float dx = pos.x - rect.x;
        float dy = pos.y - rect.y;
//        Draw.rectGdx(region_, dx, dy, rect.width, rect.height);
        Draw.rectGdx(region_, pos.x, pos.y, rect);

        if (entity.items.total() != 0) {
            int frame = goodsAniFrame(entity);
            region_ = aniRegions[curVariant][frame];
            rect = aniRects[curVariant][frame];
            dx = pos.x - rect.x;
            dy = pos.y - rect.y;
//            Draw.rectGdx(region_, dx, dy, rect.width, rect.height);
            Draw.rectGdx(region_, pos.x, pos.y, rect);
        }
    }

    /**仓库块动画帧*/
    private int goodsAniFrame(TileEntity entity) {
        float valuePer = entity.items.total() / (float)itemCapacity[entity.level()];
        if(valuePer > 1) valuePer = 1;
//        int value = (int) (valuePer * 4) + 1;
        return ((int) (valuePer * 4) + 1) % 5;
    }

    @Override
    public void drawBackground(Tile tile) {
        float dx = tile.drawxIso() - bgRect.x;
        float dy = tile.drawyIso() - bgRect.y;
//        Draw.rectGdx(bgRegion, dx, dy, bgRect.width, bgRect.height);
        Draw.rectGdx(bgRegion, tile.drawxIso(), tile.drawyIso(), bgRect);
    }

    /* 添加弹药选择功能*/
    @ZAdd
    @Override
    public <T extends OperationAction> ObjectMap<OperationAction, Cons<T>> getOperationActions(TileEntity tileEntity) {
        ObjectMap<OperationAction, Cons<T>> operationActions = super.getOperationActions(tileEntity);
        // 添加配置功能
        operationActions.put(OperationAction.SETTING, nullValue -> {
            Vars.ui.settingComp.show(this, tileEntity);
            control.input.frag.config.hideConfig();
        });
        return operationActions;
    }

    @Override
    public void displayInfo(Table table){
        SettingDisplay.displayGranary(table, this, (GranaryIsoEntity) table.getUserObject());
    }


    /**
     *  存储块 瓦砾实体
     * */
    public class GranaryIsoEntity extends StorageBlockIsoEntity {
        /** 用户自定义配置接收物品数量*/
        public ItemModuleCustom acceptItemConfig = new ItemModuleCustom();


        @Override
        public void added() {
            super.added();
            acceptItemConfig.itemCapacity = block.itemCapacity[level()];
            for (Item item : systemItems.getGranaryAccept()) {
                acceptItemConfig.setUnit(item, acceptItemConfig.maxUnit);

            this.items.addListener(Vars.systemItems.granaryItemTelegraph)
                    .addListener(Vars.systemItems.allItemTelegraph);
            }
        }

        /** 接收物品数量*/
        @Override
        @ZAdd
        public int acceptCount(Item item, int amount) {
            return Math.min(amount, this.block.itemCapacity[level()] - items.total());
        }
    }
}
