package z.world.blocks.storage;

import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.EnumSet;
import arc.struct.ObjectMap;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate.ZAdd;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.type.TileEntity;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import z.tools.serialize.XmlSerialize;
import z.ui.SettingDisplay;
import z.utils.ShapeRenderer;
import z.world.blocks.OperationAction;
import z.world.modules.ItemModuleCustom;
import z.world.modules.listener.ItemTelegraph;
import z.world.modules.listener.WarehouseItemTelegraph;

import static arc.z.util.ISOUtils.TILE_HEIGHT;
import static arc.z.util.ISOUtils.TILE_WIDTH;
import static mindustry.Vars.control;
import static mindustry.Vars.systemItems;
import static z.debug.ZDebug.disable_itemFilter;

/**
 *  仓库
 * */
public class WarehouseIso extends StorageBlockIso{
    private final int[] cropOffsetIndex = {
            /**背景偏移*/1,-1, /**货物偏移*/1,0, 1,1, 0,-1, 0,0, 0,1, -1,-1, -1,0, -1,1,
    };
    private final int[] cropDrawIndex = {
            4, 3, 0, 1, 2,
    };
    private final ObjectMap<Item, Integer> goodsAniType = ObjectMap.of(
//            Items.copper, 0,    // 保护数据
            Items.wheat, 1,
            Items.vegetables, 2,
            Items.fruit, 3,
            Items.olives, 4,
            Items.vines, 5,
            Items.meat, 6,
            Items.wine, 7,
            Items.oil, 8,
            Items.iron, 9,
            Items.timber, 10,
            Items.clay, 11,
            Items.marble, 12,
            Items.weapons, 13,
            Items.furniture, 14,
            Items.pottery, 15,
            Items.fish, 16
    );


    /** 货物纹理*/
    private TextureRegion[][] goods = new TextureRegion[17][4];

    public WarehouseIso(String name){
        super(name);
        solid = true;
        update = true;
        destructible = true;
        // zones test code begon
        flags = EnumSet.of(BlockFlag.warehouse);
        entityType = WarehouseIsoEntity::new;
        layer2 = null;  // 禁止绘制工作状态图表
//        extendedInformation = true;
        // zones test code end
    }

    private String configFile;
    public WarehouseIso(String name, String xmlFile){
        this(name);
        this.configFile = xmlFile;
        Vars.xmlSerialize.loadBlockConfig(xmlFile, this);
    }

    @Override
    public void load(){
        super.load();

        ObjectMap<String, Object> tempPool = Vars.xmlSerialize.loadBlockAnimation(this.configFile);
        if (tempPool == null)   return;
        goods = (TextureRegion[][]) tempPool.get(XmlSerialize.aniRegions);  // 货物动画初始化
        tempPool.clear();   // no must
    }

    @Override
    public void update(Tile tile) {
        super.update(tile);

        WarehouseIsoEntity entity = tile.ent();
        if(!entity.setWorkState || !entity.working)   // 无法执行工作 退出
            return;

        if(tile.entity.timer.get(timerWorker, workerTime * 2) && entity.offerWorker()){     //  移除清空物品
            for (ItemStack itemStack : entity.warehouseContent) {
                if (itemStack.amount > 0 && entity.acceptItemConfig.stateClear(itemStack.item)) {
//                    tryDump(tile, itemStack.item);
//                    entity.removeItem(itemStack.item, 1);
                    return;
                }
            }
        }
    }

    @Override
    public void draw(Tile tile){
        ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);
        WarehouseIsoEntity entity = tile.ent();

        int index = 0;
        Vec2 pos = ISOUtils.tileToWorldCoords(tile.x + cropOffsetIndex[index++], tile.y + cropOffsetIndex[index++]);
        float dx = pos.x + (region.getWidth() * Draw.getScl() - (1) * TILE_WIDTH) / 2f;
        float dy = pos.y + (region.getHeight() * Draw.getSclH() - (1) * TILE_HEIGHT) / 2f;
        Draw.rectScaleH(region, dx, dy);

        // 货物绘制
        for(int i = 0; i < 8; i++) {
            pos = ISOUtils.tileToWorldCoords(tile.x + cropOffsetIndex[index++], tile.y + cropOffsetIndex[index++]);
//            int cropIndex = entity.cropAniIndex[cropDrawIndex[i]];
            TextureRegion _region = goods[goodsAniType(entity, i)][goodsAniFrame(entity, i)];
            dx = pos.x + (_region.getWidth() * Draw.getScl() - (1) * TILE_WIDTH) / 2f;
            dy = pos.y + (_region.getHeight() * Draw.getSclH() - (1) * TILE_HEIGHT) / 2f;
            Draw.rectScaleH(_region, dx, dy);
        }
    }
    /**仓库块动画类型*/
    private int goodsAniType(WarehouseIsoEntity entity, int index) {
        int value = 0;
        try {
            value = goodsAniType.get(entity.warehouseContent[index].item);
        } catch (Exception e) {
//            System.out.println(entity.warehouseContent[index].item.name);
        }
        return entity.warehouseContent[index].amount == 0 ? 0 : value;
    }
    /**仓库块动画帧*/
    private int goodsAniFrame(WarehouseIsoEntity entity, int index) {
        float valuePer = entity.warehouseContent[index].amount / (float)entity.getMaximumAcceptedWarehouseSolt();
        if(valuePer > 1) valuePer = 1;
        return (int) (valuePer * 3);
    }


    @ZAdd
    @Override
    public boolean acceptItemCaesar(Item item, Tile tile, Tile source){
        ItemModuleCustom acceptItemConfig = ((WarehouseIsoEntity) tile.ent()).acceptItemConfig;       // no exception check
        return acceptItem(item, tile, source)
                && acceptItemConfig.stateAccept(item)
                && tile.entity.items.get(item) < acceptItemConfig.get(item)
                ;
    }

    @ZAdd
    @Override
    public boolean getItemCaesar(Item item, Tile tile, Tile source){
        ItemModuleCustom acceptItemConfig = ((WarehouseIsoEntity) tile.ent()).acceptItemConfig;       // no exception check
        return acceptItem(item, tile, source)
                && acceptItemConfig.stateGet(item)
                && tile.entity.items.get(item) < acceptItemConfig.get(item)
                ;
    }

    /** 是否可接收指定物品*/
    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return (disable_itemFilter || consumes[tile.entity.level()].itemFilters.get(item.id))
                && systemItems.getWarehouseAccept().contains(item)
                && tile.entity.items.total() < getMaximumAccepted(tile, item)
                // 仓库增加类别检测
                && ((WarehouseIsoEntity) tile.ent()).acceptWarehouse(item);
    }

    /** 仓库数据将会进行类别区分*/
    @Override
    public int getMaximumAccepted(Tile tile, Item item){
        return itemCapacity[tile.entity.level()];
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
        SettingDisplay.displayWarehouse(table, this, (WarehouseIsoEntity) table.getUserObject());
    }

    // zones add Block begon
    @Override
    @ZAdd
    protected void attributeUpgrade(TileEntity entity) {
        super.attributeUpgrade(entity);
        {   // 更新仓储物品
            // 配置数据更新
            WarehouseIsoEntity warehouseEntity = (WarehouseIsoEntity) entity;
            warehouseEntity.acceptItemConfig.updateCapacity(itemCapacity[entity.level()]);    // 配置接收最大物品数据   // 配置物品接收的最大单位数量

            // 仓库槽物品清除
            for (int i = warehouseEntity.warehouseContent.length; --i >= 0;) {     // 清除先前数据
                warehouseEntity.warehouseContent[i].amount = 0;
            }

            // 仓库槽物品重新分配
            for (Item item : systemItems.getWarehouseAccept()) {
                warehouseEntity.itemTelegraph.addTelegraph(item, entity.items.get(item));
            }
        }
    }
    // zones add Block end

    /**
     *  存储块 瓦砾实体
     * */
    public class WarehouseIsoEntity extends StorageBlockIsoEntity {
        @Deprecated
        private byte maxUnit = 8;
        /** 仓库块接收的物品*/
        public ItemStack[] warehouseContent = new ItemStack[maxUnit];

        /**仓库槽添加监听器*/
        private ItemTelegraph itemTelegraph;
        /** 用户自定义配置接收物品数量*/
        public ItemModuleCustom acceptItemConfig = new ItemModuleCustom();

        public WarehouseIsoEntity() {
            for (int i = warehouseContent.length; --i >= 0;) {      // 初始化仓库槽
                warehouseContent[i] = new ItemStack(null, 0);
            }
        }

        /** update false时初始化设置*/
        @Override
        public TileEntity init(Tile tile, boolean shouldAdd) {
            super.init(tile, shouldAdd);
            acceptItemConfig.itemCapacity = block.itemCapacity[level()];    // 配置接收最大物品数据
            for (Item item : systemItems.getWarehouseAccept()) {            // 配置物品接收的最大单位数量
                acceptItemConfig.setUnit(item, maxUnit);
            }

            return this;
        }

        @Override
        public void added() {       // update must be true
            this.items.addListener(Vars.systemItems.allItemTelegraph)
                    .addListener(Vars.systemItems.warehouseItemTelegraph)
                    .addListener(itemTelegraph = new WarehouseItemTelegraph(this));

            // debug begon
//            this.items.add(Items.wheat, 7);
//            this.items.add(Items.pottery, 7);
//            this.items.add(Items.weapons, 7);
//            this.items.add(Items.fish, 7);
//            this.items.add(Items.fruit, 7);
//            this.items.add(Items.meat, 7);
//            this.items.add(Items.olives, 7);
//            this.items.add(Items.oil, 7);
            // debug end
        }

        /** 单个存储槽接收的最大物品数量*/
        public int getMaximumAcceptedWarehouseSolt() {
            return itemCapacity[level()] / 8;
        }

        /** 仓库是否有剩余块接收新类型物品*/
        private boolean acceptWarehouse(Item item) {
            for (ItemStack itemStack : warehouseContent) {
                if (itemStack.amount == 0
                        || (itemStack.item == item && itemStack.amount < getMaximumAcceptedWarehouseSolt()) ) {
                    return true;
                }
            }
            return false;
        }

        /** 接收物品数量*/
        @Override
        @ZAdd
        public int acceptCount(Item item, int amount) {
            int removeCount = 0;
            int maxAcceptSolt = getMaximumAcceptedWarehouseSolt();
            for (int i = warehouseContent.length; --i >= 0;) {
                ItemStack itemStack = warehouseContent[i];

                if (itemStack.amount == 0
                        || (itemStack.item == item && itemStack.amount < maxAcceptSolt) ) {

                    removeCount += maxAcceptSolt - itemStack.amount;
                    if (removeCount >= amount)
                        return amount;
                }
            }

            return removeCount;
        }
    }
}
