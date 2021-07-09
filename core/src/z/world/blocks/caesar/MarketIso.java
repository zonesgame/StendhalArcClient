package z.world.blocks.caesar;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.struct.Array;
import arc.struct.EnumSet;
import arc.struct.ObjectMap;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate.ZAdd;
import arc.z.util.ZonesAnnotate.ZField;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.entities.type.TileEntity;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.meta.BlockFlag;
import z.entities.ani.SpriteAniControl;
import z.tools.serialize.XmlSerialize;
import z.utils.FinalCons;
import z.utils.ShapeRenderer;
import z.world.blocks.storage.StorageBlockIso;
import z.world.modules.ItemModuleCustom;

import static arc.z.util.ISOUtils.TILE_HEIGHT;
import static arc.z.util.ISOUtils.TILE_WIDTH;
import static z.debug.ZDebug.disable_itemFilter;

/**
 *  市场 提供巡逻工人块
 * */
public class MarketIso extends StorageBlockIso {
    // test begon
    protected final int timerTest = timers++;
    protected final int testTime = (int) (FinalCons.second * 12);       // zones editor default final
    // test end

    /** 接收物品类型*/
    @ZField
    public Array<ItemStack> needItems = new Array<>(0);
//            Array.withArrays(
//            new ItemStack(Items.wheat, 2),
//            new ItemStack(Items.vegetables, 2),
//            new ItemStack(Items.furniture, 2),
//            new ItemStack(Items.fruit, 2),
//            new ItemStack(Items.oil, 2),
//            new ItemStack(Items.wine, 2),
//            new ItemStack(Items.meat, 2),
//            new ItemStack(Items.pottery, 2)
//    );
    /** 存储生产单位倍数*/
    @ZField
    public int [] consumesMultiple = {3};      // zones add
    // 获取物品
    ConsumeItems items;

    private int[] offset;
    /** 动画播放控制器*/
    private SpriteAniControl aniControl;
    /** 动画纹理*/
    private TextureRegion[][] aniRegions;

    public MarketIso(String name){
        super(name);
        solid = true;
        update = true;
        destructible = true;
        // zones test code begon
        flags = EnumSet.of(BlockFlag.producer);
//        acceptType.sort(Structs.comparingInt(i -> i.id));       // ui显示数据排序
        entityType = MarketIsoEntity::new;
        // zones test code end
    }

    private String configFile;
    public MarketIso(String name, String xmlFile){
        this(name);
        this.configFile = xmlFile;
        Vars.xmlSerialize.loadBlockConfig(xmlFile, this);
    }

    @Override
    public void load(){
        super.load();
        ObjectMap<String, Object> tempPool = Vars.xmlSerialize.loadBlockAnimation(this.configFile);
        if (tempPool == null)   return;
        aniRegions = (TextureRegion[][]) tempPool.get(XmlSerialize.aniRegions);
        offset = (int[]) tempPool.get(XmlSerialize.offset);
        tempPool.clear();   // no must

        aniControl = new SpriteAniControl().setFrameData(aniRegions[0].length, 10);
    }

    @ZAdd
    @Override
    public boolean acceptItemCaesar(Item item, Tile tile, Tile source){
        ItemModuleCustom acceptItemConfig = ((MarketIsoEntity) tile.ent()).acceptItemConfig;       // no exception check
        return acceptItem(item, tile, source)
                && acceptItemConfig.stateAccept(item)
                && tile.entity.items.get(item) < acceptItemConfig.get(item);
    }

    @ZAdd
    @Override
    public boolean getItemCaesar(Item item, Tile tile, Tile source){
        ItemModuleCustom acceptItemConfig = ((MarketIsoEntity) tile.ent()).acceptItemConfig;       // no exception check
        return acceptItem(item, tile, source)
                && acceptItemConfig.stateGet(item)
                && tile.entity.items.get(item) < acceptItemConfig.get(item);
    }

    /** 是否可接收指定物品*/
    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return (disable_itemFilter || consumes[tile.entity.level()].itemFilters.get(item.id))
//                && acceptType.contains(item)
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

        // 更新获取生产物品. zones add begon
        MarketIsoEntity entity = tile.ent();
        if( (!entity.working || true) && tile.entity.timer.get(timerTest, testTime) && entity.offerWorker()){
            tryPatrol(tile);
        }
        if(true && tile.entity.timer.get(timerWorker, workerTime) && entity.offerWorker()){
            Array needItems = getObtainItems(entity);
            if (needItems.size > 0 ) {
                tryObtain(tile, needItems, UnitTypes.marketWorker);
            }
        }
    }

    @Override
    public void draw(Tile tile){
        ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);

        Vec2 pos = ISOUtils.tileToWorldCoords(offsetTile() + tile.x , offsetTile() + tile.y );
        float dx = pos.x + (region.getWidth() * Draw.getScl() - (size) * TILE_WIDTH) / 2f;
        float dy = pos.y + (region.getHeight() * Draw.getSclH() - (size) * TILE_HEIGHT) / 2f;
        Draw.rectScaleH(region, dx, dy);
        // 动画绘制
        MarketIsoEntity entity = tile.ent();
        if (entity.working) {
            Draw.rectScaleH(aniRegions[0][aniControl.getTimeFrame()], dx + offset[0] * Draw.getScl(), dy + offset[1] * Draw.getScl());
        }
    }

    @Override
    public void removed(Tile tile) {
        super.removed(tile);
    }

    /** Market使用*/
//    private ItemStack getNeedItem(MarketIsoEntity entity) {
//        ItemModule allItems = Vars.systemItems.allItems;
//        for (int i = 0; i < needItems.size; i++) {
//            ItemStack itemStack = needItems.get(i);
//            if (entity.items.get(itemStack.item) < itemStack.amount && allItems.get(itemStack.item) >= itemStack.amount) {
//                return itemStack;
//            }
//        }
//        for (int i = 0; i < needItems.size; i++) {
//            ItemStack itemStack = needItems.get(i);
//            if (entity.items.get(itemStack.item) < itemStack.amount * 3 && allItems.get(itemStack.item) >= itemStack.amount) {
//                return itemStack;
//            }
//        }
//
//        return null;
//    }

    private int  giveInventory(Array<ItemStack> obtainItems, ItemStack addObtain) {
        int alreadyOwned = addObtain.amount;
        for (ItemStack itemStack : obtainItems) {
            if (itemStack.item == addObtain.item)
                alreadyOwned += itemStack.amount;
        }

        int inventoryCount = Vars.systemItems.granaryItems.get(addObtain.item);
        inventoryCount += Vars.systemItems.warehouseItems.get(addObtain.item);

        int value = Math.max(0 , inventoryCount >= alreadyOwned ? addObtain.amount : inventoryCount - alreadyOwned - addObtain.amount);
        return value;
    }

    @ZAdd
    private Array<ItemStack> getObtainItems(TileEntity entity) {
        Array<ItemStack> obtainItems = super.getDumpItems(entity);

        {
            int multiple = 1;
            int obtainAmount = carryItemObtain[entity.level()];
            while (obtainAmount > 0) {
                for (ItemStack itemStack : needItems) {
                    float craftPer = entity.items.get(itemStack.item) / (float)itemStack.amount;
                    if (craftPer < multiple) {
                        int minAmount = Math.min(giveInventory(obtainItems, itemStack), obtainAmount);
                        if (minAmount > 0)
                            obtainItems.add(new ItemStack(itemStack.item, minAmount));

                        obtainAmount -= minAmount;
                        if (obtainAmount <= 0)
                            break;
                    }
                }

                multiple++;
                if (multiple >= consumesMultiple[entity.level()] + 1) break;
            }

            // 合并重复物品项
            for (int i = 0; i < obtainItems.size - 1; i++) {

                for (int j = i + 1; j < obtainItems.size; j++) {
                    if (obtainItems.get(i).item == obtainItems.get(j).item) {
                        obtainItems.get(i).amount += obtainItems.get(j).amount;
                        obtainItems.remove(j);
                        j--;
                    }
                }
            }
        }

        return obtainItems;
    }


    /**
     *  存储块 瓦砾实体
     * */
    public class MarketIsoEntity extends StorageBlockIsoEntity {
//        private int maxWorker = 1;
//        Array<BlockUnit> worker = new Array<>(maxWorker);

        /** 用户自定义配置接收物品数量*/
        private ItemModuleCustom acceptItemConfig = new ItemModuleCustom();

        public MarketIsoEntity() {
//            for (int i = maxWorker; --i >= 0;) {
//                worker.add(Vars.workerPool.obtain());
//            }
//            for (Item item : acceptType) {
//                acceptItemConfig.set(item, 5);
//            }
        }

        @Override
        public void removed() {
            super.removed();
        }

        @Override
        @ZAdd
        public int acceptCount(Item item, int amount) {
            return amount;
        }
    }
}
