package z.world.blocks.caesar;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import arc.Core;
import arc.func.Prov;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Interval;
import arc.util.Time;
import arc.z.util.ISOUtils;
import arc.z.util.ZonesAnnotate.ZAdd;
import arc.z.util.ZonesAnnotate.ZField;
import arc.z.util.ZonesAnnotate.ZMethod;
import mindustry.Vars;
import mindustry.entities.type.TileEntity;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Bar;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.meta.BlockGroup;
import mindustry.world.modules.ItemModule;
import z.debug.Strs;
import z.entities.type.base.BlockUnit;
import z.tools.serialize.XmlSerialize;
import z.utils.FinalCons;
import z.utils.ShapeRenderer;
import z.world.blocks.defense.EditorBlock;

import static arc.z.util.ISOUtils.TILE_HEIGHT;
import static arc.z.util.ISOUtils.TILE_WIDTH;
import static mindustry.Vars.systemWorker;

/**
 *
 */
public class HousingIso extends EditorBlock {
    /***/
    public Prov<TextureRegion[]> drawIcons = null;
    // 纹理数据zones add begon
    /***/
    /** 动画纹理*/
    private TextureRegion[][] aniRegions;
    /** 粘土坑淹没纹理*/
    private TextureRegion[] storageRegions;
    /** 状态图表*/
    private TextureRegion noWorkingIco;
    // zones add end
    /** 升级所需物品*/
    private ConsumeItems[] itemConsume;
    /** 物品存储倍数*/
    @Deprecated
    private int[] storageMultiplier = {2, 3, 3, 4, 5};

    /** 物品使用时间*/
    @ZField
    public float craftTime[];    // default 60 * 8
    /** 提供农民数量*/
    @ZField
    public int[] offerPeasant = {};
    /** 提供工人数量*/
    @ZField
    public int[] offerWorker = {};
    /** 提供基础的保护数据*/
    @ZField
    public int baseOfferPeasant;
    @ZField
    public int baseOfferWorker;

    public HousingIso(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
//        sync = true;
        entityType = HousingIsoEntity::new;
        layer2 = Layer.power;

        solid = true;
        destructible = true;
        group = BlockGroup.walls;
        buildCostMultiplier = 5f;
    }

    public HousingIso(String name, String configFile){
        this(name);
        this.configFile = configFile;
        // 加载配置文件数据
        Vars.xmlSerialize.loadBlockConfig(configFile, this);
    }

    @Override
    public void setStats(){
        super.setStats();
    }

    @Override
    public void init(){
        super.init();
    }

    @Override
    public void setBars(){
        bars.add("level", entity -> new Bar(() -> Core.bundle.format(Strs.str22, entity.level() + 1), () -> Pal.lancerLaser, entity::levelf).blink(Color.white));
        super.setBars();
        // 添加房屋专用状态条
//        bars.remove("items");   // not must
        bars.add("items", entity -> new Bar(() -> Core.bundle.format("bar.items", entity.items.total()), () -> Pal.items, () -> (float)entity.items.total() / getMaximumAccepted(entity)));
//        bars.add("items", entity -> new Bar(() -> Core.bundle.format("bar.items", entity.items.total()), () -> Pal.items, () -> (float)entity.items.total() / itemCapacity));
    }

    /** 提供农民数量*/
    public int getOfferPeasant(HousingIsoEntity entity) {
        return entity.working ? offerPeasant[entity.levle] + baseOfferPeasant : baseOfferPeasant;
    }

    /** 提供工人数量*/
    public int getOfferWorker(HousingIsoEntity entity) {
        return entity.working ? offerWorker[entity.levle] + baseOfferWorker : baseOfferWorker;
    }

    @Override
    public void draw(Tile tile){
        ShapeRenderer.drawDiamond(tile.x, tile.y, tile.block().size);

        HousingIsoEntity entity = tile.ent();
        TextureRegion region;
        if (entity.levle <= 1) {
            region = aniRegions[5][0];
            Vec2 pos = ISOUtils.tileToWorldCoords(offsetTile() + tile.x , offsetTile() + tile.y );
            float dx = pos.x + (region.getWidth() * Draw.getScl() - (size) * TILE_WIDTH) / 2f;
            float dy = pos.y + (region.getHeight() * Draw.getSclH() - (size) * TILE_HEIGHT) / 2f;
            Draw.rectScaleH(region, dx, dy);
        }

        region = aniRegions[entity.levle][entity.variants];
        Vec2 pos = ISOUtils.tileToWorldCoords(offsetTile() + tile.x , offsetTile() + tile.y );
        float dx = pos.x + (region.getWidth() * Draw.getScl() - (size) * TILE_WIDTH) / 2f;
        float dy = pos.y + (region.getHeight() * Draw.getSclH() - (size) * TILE_HEIGHT) / 2f;
        Draw.rectScaleH(region, dx, dy);
    }

    /** 绘制未工作状态图标*/
    @Override
    public void drawLayer2(Tile tile) {
        // 绘制升级状态图标 begon
        HousingIsoEntity entity = tile.ent();

        if ( !entity.working) {
            float dx = tile.drawxIso();
            float dy = tile.drawyIso();
            float width1 = noWorkingIco.getWidth() * Draw.scl * 2;
            float height1 = noWorkingIco.getHeight() * Draw.scl * 2;
            Draw.rectGdx(noWorkingIco, dx - width1 / 2f, dy - height1 / 2f, width1, height1);
        }
    }

    @Override
    public TextureRegion[] generateIcons(){
        return drawIcons == null ? super.generateIcons() : drawIcons.get();
    }

    @Override
    public void update(Tile tile){
        super.update(tile);
        HousingIsoEntity entity = tile.ent();

        if(entity.cons.valid()){
            entity.progress += getProgressIncrease(entity, craftTime[entity.level()]);
            entity.totalProgress += entity.delta();
        }

        if(entity.progress >= 1f){
            entity.working =false;
            entity.progress = 0f;

            int nextLev = entity.getNextLev();
            for(int i = nextLev; i >= 0; i--) {
                if (itemConsume[i].valid(entity)) {
                    itemConsume[i].trigger(entity);
                    entity.level(i);
                    entity.working = true;
                    break;
                }
            }

            if( !entity.working) {
              entity.level(0);
            }
        }

        systemWorker.processOffer(tile);
    }

    /** 接收的最大总物品数量*/
    private int getMaximumAccepted(TileEntity entity){
        int maxCapacity = 0;
        ConsumeItems needItems = itemConsume[entity.level()];
        int multiplier = storageMultiplier[entity.level()];
        for(ItemStack stack : needItems.items){
            maxCapacity += stack.amount * multiplier;
        }

        return maxCapacity;
    }

//    /** 接收指定物品数量*/
//    private int getMaximumAcceptedItem(TileEntity entity, Item item){
//        ConsumeItems needItems = itemConsume[entity.level()];
//        for(ItemStack stack : needItems.items){
//            if(stack.item.name.equals(item.name))
//                return stack.amount * storageMultiplier[entity.level()];
//        }
//
//        return 0;
//    }

//    public void addItem(Tile tile) {
//    }

    private float stepPatrol = FinalCons.second * 5;
    /** 更新巡逻添加物品*/
    public void updatePatrol(Tile tile, BlockUnit unit) {
        if ( !(tile.ent() instanceof HousingIsoEntity)) return;

        ItemModule marketItems = unit.getMarketItems();
        if(marketItems == null) return;
        HousingIsoEntity entity = tile.ent();
        float time = Time.time();
//        float timeNull = 0f;

        if (time - entity.patrolUnitCatch.get(unit, 0f) > stepPatrol) {
            entity.patrolUnitCatch.put(unit, time);

            ConsumeItems needItems = itemConsume[entity.getNextLev()];
            for(ItemStack stack : needItems.items){
                int amount = Math.min(stack.amount, marketItems.get(stack.item));       // debug + 4
                {
                    int maximumAcceptedItem = stack.amount * storageMultiplier[entity.getNextLev()];
                    amount = Math.min(maximumAcceptedItem - entity.items.get(stack.item), amount);
                    if(amount < 0) amount = 0;
                }
                marketItems.remove(stack.item, amount);
                entity.items.add(stack.item, amount);
            }
        }

        if( entity.interval.get(entity.timerPatrol, stepPatrol)){   // 清除过期数据
            entity.clearUnitCatch(time, stepPatrol);
        }
    }

    @Override
    public void load(){
        super.load();
        this.noWorkingIco = Core.atlas.find("upgrade6");
        // zones add begon
        ObjectMap<String, Object> tempPool = Vars.xmlSerialize.loadBlockAnimation(this.configFile);
        if (tempPool == null)   return;
        aniRegions = (TextureRegion[][]) tempPool.get(XmlSerialize.aniRegions);
        offset = (int[]) tempPool.get(XmlSerialize.offset);
        tempPool.clear();   // no must
        // zones add end
    }

    @Override
    public void displayConsumption(Tile tile, Table table){
        table.left();
//        for(Consume cons : consumes.all()){
//            if(cons.isOptional() && cons.isBoost()) continue;
//            cons.build(tile, table);
//        }
        HousingIsoEntity entity = tile.ent();
        this.itemConsume[entity.level()].build(tile, table);
    }

    @Override
    public boolean canReplace(Block other){
        return super.canReplace(other) && health[0] > other.health[0] && false;
    }

//    @ZAdd
//    @Override
//    public <T extends OperationAction> ObjectMap<OperationAction, Cons<T>> getOperationActions(TileEntity tileEntity) {
//        ObjectMap<OperationAction, Cons<T>> operationActions = super.getOperationActions(tileEntity);
//        operationActions.put(OperationAction.EXTENDEDINFORMATION, null);
//        return operationActions;
//    }

    @ZMethod
    public void needItemConsume(ItemStack[]... items) {
        itemConsume = new ConsumeItems[items.length];
        for (int i = items.length; --i >=0;) {
            itemConsume[i] = new ConsumeItems(items[i]);
        }
    }

    // zones add begon
    private String configFile;
    private int[]offset;


    /**
     *  房屋 瓦砾实体
     * */
    public static class HousingIsoEntity extends TileEntity {
        private int levle = -1, maxLev;
        private int variants = 0;

        /** 处理进度*/
        public float progress;
        /** 总进度*/
        public float totalProgress;
        public boolean working = false;

        private Interval interval;
        private int timerFinal = 0;
        private int timerPatrol = timerFinal++;

        private ObjectMap<BlockUnit, Float> patrolUnitCatch = new ObjectMap<>(8);   // default 4

        HousingIsoEntity() {
            interval = new Interval(timerFinal);
        }

        @Override
        public TileEntity init(Tile tile, boolean shouldAdd) {
            super.init(tile, shouldAdd);

            HousingIso housingBlock = (HousingIso) this.block;
            interval.check(timerPatrol, housingBlock.stepPatrol);
            maxLev = housingBlock.itemConsume.length - 1;

            level(0);
            return this;
        }

//        @Override
//        public void added() {
//            HousingIso housingBlock = (HousingIso) this.block;
//            interval.check(timerPatrol, housingBlock.stepPatrol);
//            maxLev = housingBlock.itemConsume.length - 1;
//
//            setLevle(0);
//        }

        @ZAdd
        private void addItem(Item item, int amount) {
            this.items.add(item, amount);
        }

        @ZAdd
        private void removeItem(Item item, int amount) {
            this.items.remove(item, amount);
        }

        @Override
        public void write(DataOutput stream) throws IOException {
            super.write(stream);
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
        }

        private void clearUnitCatch(float time, float steppatrol) {
            for(BlockUnit catchUnit : patrolUnitCatch.keys()) {
                if(time - patrolUnitCatch.get(catchUnit, 0f) > steppatrol) {
                    patrolUnitCatch.remove(catchUnit);
                }
            }
        }

        // LevelTrait begon
        @Override
        public int level() {
//            return this.levle;
            return Mathf.clamp(this.levle, 0, maxLevel());
        }

        @Override
        public int maxLevel() {
            return maxLev;
        }

        /** 获取升级下一等级*/
        private int getNextLev() {
            return copyLevelBy(1);
        }

        @Override
        public void level(int lev) {
            if (this.levle == lev)   return;
            {   // 更新等级配置数据
                float healthPercent = healthf();    //  healthf execute health() / maxHealth(); 确保数据没被重置前调用
//                block.health = block.healthExtend[this.levle];
                this.health(block.health[lev] * healthPercent);
            }

            HousingIso block = (HousingIso) this.block;
            levle = lev;
            variants = Mathf.random(block.aniRegions[levle].length - 1);
        }
        // LevelTrait end
    }
}
